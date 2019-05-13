package de.uni_potsdam.hpi.openmensa.api.client

interface PagedApi<T> {
    companion object {
        const val MAX_PAGE_LENGTH = 100
        const val FIRST_PAGE_INDEX = 1
    }

    fun query(limit: Int, page: Int): PagedResponse<T>
}