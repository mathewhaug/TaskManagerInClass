package com.matthaug.taskmanager.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.matthaug.taskmanager.models.Task

class TaskPagingSource(
    private val allTasks: List<Task>
) : PagingSource<Int, Task>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Task> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        val fromIndex = page * pageSize
        val toIndex = kotlin.math.min(fromIndex + pageSize, allTasks.size)

        val pageData = if (fromIndex < allTasks.size) {
            allTasks.subList(fromIndex, toIndex)
        } else {
            emptyList()
        }

        return LoadResult.Page(
            data = pageData,
            prevKey = if (page == 0) null else page - 1,
            nextKey = if (toIndex < allTasks.size) page + 1 else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Task>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
