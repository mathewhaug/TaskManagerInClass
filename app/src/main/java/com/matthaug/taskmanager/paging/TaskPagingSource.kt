package com.matthaug.taskmanager.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.matthaug.taskmanager.models.Task

class TaskPagingSource(private val allTasks: List<Task>) : PagingSource<Int, Task>() {

    override fun getRefreshKey(state: PagingState<Int, Task>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val page = state.closestPageToPosition(anchorPosition)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Task> {
        val page = params.key ?: 1
        val pageSize = params.loadSize
        val fromIndex = (page - 1) * pageSize
        val toIndex = kotlin.math.min(fromIndex + pageSize, allTasks.size)

        return try {
            val tasks = if (fromIndex >= allTasks.size) emptyList()
            else allTasks.subList(fromIndex, toIndex)

            LoadResult.Page(
                data = tasks,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (toIndex < allTasks.size) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
