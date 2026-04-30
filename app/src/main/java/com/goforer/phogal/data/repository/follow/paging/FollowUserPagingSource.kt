package com.goforer.phogal.data.repository.follow.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User

import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class FollowUserPagingSource(
    private val followedUsers: List<User>
) : PagingSource<Int, User>() {

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val fromIndex = page * pageSize
            val toIndex = minOf(fromIndex + pageSize, followedUsers.size)

            val data = if (fromIndex < followedUsers.size) {
                followedUsers.subList(fromIndex, toIndex)
            } else {
                emptyList()
            }

            LoadResult.Page(
                data = data,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (toIndex >= followedUsers.size) null else page + 1
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