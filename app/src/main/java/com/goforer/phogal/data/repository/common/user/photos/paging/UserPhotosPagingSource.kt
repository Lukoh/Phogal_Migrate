package com.goforer.phogal.data.repository.common.user.photos.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.goforer.phogal.BuildConfig
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.model.remote.response.gallery.common.Photo
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class UserPhotosPagingSource(
    private val api: RestAPI,
    private val username: String,
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
            val response = api.getUserPhotos(
                username = username,
                clientId = BuildConfig.clientId,
                page = page,
                perPage = pageSize
            )
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val photos = response.body().orEmpty()
            LoadResult.Page(
                data = photos,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (photos.isEmpty()) null else page + 1
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
