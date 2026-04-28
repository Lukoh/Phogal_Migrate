package com.goforer.phogal.data.repository.bookmark.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class BookmarkPagingSource(
    private val pictures: List<Picture>
) : PagingSource<Int, Picture>() {

    override fun getRefreshKey(state: PagingState<Int, Picture>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Picture> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val fromIndex = page * pageSize
            val toIndex = minOf(fromIndex + pageSize, pictures.size)

            val data = if (fromIndex < pictures.size) {
                pictures.subList(fromIndex, toIndex)
            } else {
                emptyList()
            }

            LoadResult.Page(
                data = data,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (toIndex >= pictures.size) null else page + 1
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
}