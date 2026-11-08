package com.hieu.di

import com.hieu.BuildConfig
import com.hieu.data.remote.api.IndMartApiService
import com.hieu.data.remote.api.KeycloakApiService
import com.hieu.data.remote.interceptor.AuthInterceptor
import com.hieu.data.remote.interceptor.TokenAuthenticator
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                // Không log token hoặc dữ liệu bảo mật trong header Authorization
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    @Named("KeycloakClient")
    fun provideKeycloakOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("KeycloakRetrofit")
    fun provideKeycloakRetrofit(
        @Named("KeycloakClient") okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.KEYCLOAK_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
    }

    @Provides
    @Singleton
    fun provideKeycloakApiService(
        @Named("KeycloakRetrofit") retrofit: Retrofit
    ): KeycloakApiService {
        return retrofit.create(KeycloakApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("IndMartClient")
    fun provideIndMartOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("IndMartRetrofit")
    fun provideIndMartRetrofit(
        @Named("IndMartClient") okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
    }

    @Provides
    @Singleton
    fun provideIndMartApiService(
        @Named("IndMartRetrofit") retrofit: Retrofit
    ): IndMartApiService {
        return retrofit.create(IndMartApiService::class.java)
    }
}

