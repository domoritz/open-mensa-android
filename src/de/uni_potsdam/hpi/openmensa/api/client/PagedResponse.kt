package de.uni_potsdam.hpi.openmensa.api.client

data class PagedResponse<T>(
        val totalPages: Int,
        val items: List<T>
)