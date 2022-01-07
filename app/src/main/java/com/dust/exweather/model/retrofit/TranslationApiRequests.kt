package com.dust.exweather.model.retrofit

import com.dust.exweather.model.dataclasses.translatedataclass.TranslationDataClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationApiRequests {
    @GET("?type=json")
    suspend fun translate(
        @Query("from") fromLang: String = "en",
        @Query("to") toLang: String = "fa",
        @Query("text") text: String
    ):Response<TranslationDataClass>
}