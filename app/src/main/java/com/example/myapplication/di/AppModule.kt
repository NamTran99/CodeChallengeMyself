package com.example.myapplication.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Cung cấp Retrofit instance
    @Provides
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

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
