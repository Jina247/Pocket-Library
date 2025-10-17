package com.jina.pocketlibrary.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


data class BookSearchResponse(
    @SerializedName("docs") val docs: List<BookDto>
)

data class BookDto(
    @SerializedName("key") val key: String?,
    @SerializedName("title") val title: String,
    @SerializedName("author_name") val authorName: List<String?>,
    @SerializedName("first_publish_year") val firstPublishYear: Int?,
    @SerializedName("cover_i") val coverId: Int?
)

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("fields") fields: String = "key, title, author_name, first_publish_year, cover_i",
        @Query("limit") limit: Int = 20
    ) : BookSearchResponse
}

object RetrofitInstance {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val api: OpenLibraryApi = retrofit.create(OpenLibraryApi::class.java)
}