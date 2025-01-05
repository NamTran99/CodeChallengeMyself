package com.example.myapplication.di

import app.swiftmail.data.helper.interceptor.CurlLoggerInterceptor
import com.example.myapplication.data.api.OpenAIService
import com.example.myapplication.data.services.MainRemoteService
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
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(httpLoggingInterceptor: HttpLoggingInterceptor): Retrofit {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(CurlLoggerInterceptor())
            .readTimeout(60L, TimeUnit.SECONDS)

        return Retrofit.Builder()
            .baseUrl("https://chatgpt-au.vulcanlabs.co")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClientBuilder.build())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIService(retrofit: Retrofit): OpenAIService {
        return retrofit.create(OpenAIService::class.java)
    }

    @Provides
    @Singleton
    fun provideMainRemoteService(openAIService: OpenAIService): MainRemoteService {
        return MainRemoteService(openAIService)
    }


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
