package de.uni_potsdam.hpi.openmensa.api.client

interface PagedApi<T> {
    companion object {
        const val MAX_PAGE_LENGTH = 100
        const val FIRST_PAGE_INDEX = 1
    }

    fun query(limit: Int, page: Int): PagedResponse<T>
}

fun <T> PagedApi<T>.queryAllItems(): List<T> {
    val limit = PagedApi.MAX_PAGE_LENGTH
    val result = mutableListOf<T>()
    var currentPage = PagedApi.FIRST_PAGE_INDEX
    var maxPages = currentPage

    while (currentPage <= maxPages) {
        query(limit = limit, page = currentPage++).let { response ->
            maxPages = response.totalPages
            result.addAll(response.items)
        }
    }

    return result
}