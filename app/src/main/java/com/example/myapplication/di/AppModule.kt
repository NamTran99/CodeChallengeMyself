package com.example.myapplication.di

import app.swiftmail.data.helper.interceptor.CurlLoggerInterceptor
import com.example.myapplication.data.api.OpenAIService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {



    // Cung cấp ApiService
    //    @Provides
    //    fun provideApiService(retrofit: Retrofit): ApiService =
    //        retrofit.create(ApiService::class.java)
    //
    //    // Cung cấp UserRepository
    //    @Provides
    //    fun provideUserRepository(apiService: ApiService): UserRepository =
    //        UserRepository(apiService)
}
