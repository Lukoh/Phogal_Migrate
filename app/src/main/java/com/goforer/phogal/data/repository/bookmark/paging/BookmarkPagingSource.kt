package com.goforer.phogal.data.repository.bookmark.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import kotlin.coroutines.cancellation.CancellationException

/**
 * Paging source for bookmarked photos stored locally.
 *
 * Since bookmarks are already loaded into memory as a [List], this source
 * simply slices the list based on the requested page size.
 *
 * This implementation uses 1-based indexing for consistency with other
 * paging sources in the project.
 */
class BookmarkPagingSource(
    private val pictures: List<Picture>
) : PagingSource<Int, Picture>() {

    override fun getRefreshKey(state: PagingState<Int, Picture>): Int? {
        return state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor) ?: return null
            page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Picture> {
        val page = params.key ?: STARTING_PAGE
        val pageSize = params.loadSize

        return try {
            val fromIndex = (page - 1) * pageSize
            val toIndex = (fromIndex + pageSize).coerceAtMost(pictures.size)

            val data = if (fromIndex in pictures.indices) {
                pictures.subList(fromIndex, toIndex)
            } else {
                emptyList()
            }

            LoadResult.Page(
                data = data,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (toIndex >= pictures.size) null else page + 1
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }

    private companion object {
        const val STARTING_PAGE = 1
    }
}
