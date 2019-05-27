package de.uni_potsdam.hpi.openmensa.api.client

import android.content.Context
import android.util.JsonReader
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.data.model.DayWithMeals
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class HttpApiClient(val serverUrl: String): ApiClient {
    companion object {
        private val httpClient: OkHttpClient by lazy { OkHttpClient() }

        private fun <T> requestPagedJson(request: Request, parseItem: (JsonReader) -> T): PagedResponse<T> {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("request was not successfully; code ${response.code()}")
                }

                val totalPages = response.headers().get("X-Total-Pages")?.toIntOrNull() ?: 1

                response.body()!!.charStream().use { stream ->
                    JsonReader(stream).use { reader ->
                        val items = mutableListOf<T>()

                        reader.beginArray()
                        while (reader.hasNext()) {
                            items.add(parseItem(reader))
                        }
                        reader.endArray()

                        return PagedResponse(
                                totalPages = totalPages,
                                items = items
                        )
                    }
                }
            }
        }

        fun getInstance(context: Context) = HttpApiClient(serverUrl = SettingsUtils.with(context).sourceUrl)
    }

    init {
        if (serverUrl.isBlank()) {
            throw IOException("no server configured")
        }
    }

    override val canteens = object: PagedApi<Canteen> {
        override fun query(limit: Int, page: Int): PagedResponse<Canteen> = requestPagedJson(
                Request.Builder()
                        .url("$serverUrl/canteens?page=$page&limit=$limit")
                        .build()
        ) { Canteen.parse(reader = it) }
    }

    override fun queryDaysWithMeals(canteenId: Int): PagedApi<DayWithMeals> = object: PagedApi<DayWithMeals> {
        override fun query(limit: Int, page: Int): PagedResponse<DayWithMeals> = requestPagedJson(
                Request.Builder()
                        .url("$serverUrl/canteens/$canteenId/meals?page=$page&limit=$limit")
                        .build()
        ) { DayWithMeals.parse(reader = it, canteenId = canteenId) }
    }
}