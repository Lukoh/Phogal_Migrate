package com.goforer.phogal.data.repository.common.user.photos.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.goforer.phogal.data.datasource.network.BackendException
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.datasource.network.fold
import com.goforer.phogal.data.datasource.network.safeApiCall
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo

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
        return safeApiCall {
            api.getUserPhotos(
                username = username,
                page = page,
                perPage = pageSize
            )
        }.fold(
            onSuccess = { photos ->
                LoadResult.Page(
                    data = photos,
                    prevKey = if (page == STARTING_PAGE) null else page - 1,
                    nextKey = if (photos.isEmpty()) null else page + 1
                )
            },
            onEmpty = {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (page == STARTING_PAGE) null else page - 1,
                    nextKey = null
                )
            },
            onError = { code, message ->
                LoadResult.Error(BackendException(code, message))
            },
            onException = { throwable ->
                LoadResult.Error(throwable)
            }
        )
    }

    private companion object {
        const val STARTING_PAGE = 1
    }
}
