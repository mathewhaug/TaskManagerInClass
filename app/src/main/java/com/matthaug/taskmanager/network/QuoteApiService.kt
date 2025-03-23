package com.matthaug.taskmanager.network

import com.matthaug.taskmanager.models.Quote
import retrofit2.http.GET

interface QuoteApiService {
    @GET("random")
    suspend fun getQuotes(): List<Quote>
}