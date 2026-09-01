package com.example.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface TolstoyApiService {
    @GET("api/books.json")
    suspend fun getLibrary(): LibraryResponse

    @GET
    suspend fun getBookContent(@Url url: String): BookContentDto

    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody
}
