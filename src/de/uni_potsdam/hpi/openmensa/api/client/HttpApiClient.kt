package de.uni_potsdam.hpi.openmensa.api.client

import android.util.JsonReader
import de.uni_potsdam.hpi.openmensa.data.model.Canteen
import de.uni_potsdam.hpi.openmensa.data.model.Day
import de.uni_potsdam.hpi.openmensa.data.model.Meal
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class HttpApiClient(val serverUrl: String): ApiClient {
    companion object {
        private val httpClient: OkHttpClient by lazy { OkHttpClient() }

        fun <T> requestPagedJson(request: Request, parseItem: (JsonReader) -> T): PagedResponse<T> {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("request was not successfully; code ${response.code()}")
                }

                val totalPages = response.headers().get("X-Total-Pages")?.toIntOrNull() ?: throw IOException("missing total pages")

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
    }

    override val canteens = object: PagedApi<Canteen> {
        override fun query(limit: Int, page: Int): PagedResponse<Canteen> = requestPagedJson(
                Request.Builder()
                        .url("$serverUrl/canteens?page=$page&limit=$limit")
                        .build()
        ) { Canteen.parse(reader = it) }
    }

    override fun queryDays(canteenId: Int): PagedApi<Day> = object: PagedApi<Day> {
        override fun query(limit: Int, page: Int): PagedResponse<Day> = requestPagedJson(
                Request.Builder()
                        .url("$serverUrl/canteens/$canteenId/days?page=$page&limit=$limit")
                        .build()
        ) { Day.parse(reader = it, canteenId = canteenId) }
    }

    override fun queryMeals(canteenId: Int, date: String): PagedApi<Meal> = object: PagedApi<Meal> {
        override fun query(limit: Int, page: Int): PagedResponse<Meal> = requestPagedJson(
            Request.Builder()
                    .url("$serverUrl/canteens/$canteenId/days/$date/meals?page=$page&limit=$limit")
                    .build()
        ) { Meal.parse(reader = it, canteenId = canteenId, date = date) }
    }
}