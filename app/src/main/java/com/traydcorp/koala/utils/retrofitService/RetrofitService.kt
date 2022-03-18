package com.traydcorp.newdio.utils.retofitService

import com.google.gson.JsonObject
import com.traydcorp.koala.dataModel.NewsDetail
import retrofit2.http.*

interface RetrofitService {

    @GET("app/topics/")
    fun getNewsList (
        @Query("category") category : String?,
        @Query("last-id") lastId : Int?
    ) : retrofit2.Call<List<NewsDetail>>

    @GET("app/topics/")
    fun getSearchNews (
        @Query("category") category : String?,
        @Query("search-word") searchWord : String?,
        @Query("last-id") lastId : Int?
    ) : retrofit2.Call<NewsDetail>


}