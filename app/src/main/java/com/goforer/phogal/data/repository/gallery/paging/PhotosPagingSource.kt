package com.goforer.phogal.data.repository.gallery.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.goforer.phogal.BuildConfig
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.model.remote.response.gallery.common.Photo
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Paging source for keyword search on Unsplash.
 *
 * Unlike the legacy `BasePagingSource`, this class takes its dependencies via the
 * constructor — there's no hidden shared state and no late-bound `Params` array.
 *
 * Error handling strategy:
 *  - [IOException]   → propagated as `LoadResult.Error` (Paging will retry)
 *  - [HttpException] → propagated as `LoadResult.Error` with the original code preserved
 *  - any other [Throwable] is also reported via `LoadResult.Error`
 *  - [CancellationException] is re-thrown to preserve structured concurrency
 */
class PhotosPagingSource(
    private val api: RestAPI,
    private val query: String,
    private val pageSize: Int
) : PagingSource<Int, Photo>() {

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        return state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor) ?: return null
            page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val page = params.key ?: STARTING_PAGE
        return try {
            val response = api.getPhotos(
                clientId = BuildConfig.clientId,
                keyword = query,
                page = page,
                perPage = pageSize
            )
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val results = response.body()?.results.orEmpty()
            LoadResult.Page(
                data = results,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (results.isEmpty()) null else page + 1
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (io: IOException) {
            LoadResult.Error(io)
        } catch (http: HttpException) {
            LoadResult.Error(http)
        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }

    private companion object {
        const val STARTING_PAGE = 1
    }
}
