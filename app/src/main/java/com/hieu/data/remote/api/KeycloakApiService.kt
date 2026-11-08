package com.hieu.data.remote.api

import com.hieu.data.remote.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface KeycloakApiService {

    @FormUrlEncoded
    @POST("realms/{realm}/protocol/openid-connect/token")
    suspend fun login(
        @Path("realm") realm: String,
        @Field("grant_type") grantType: String = "password",
        @Field("client_id") clientId: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse

    @FormUrlEncoded
    @POST("realms/{realm}/protocol/openid-connect/token")
    suspend fun refreshToken(
        @Path("realm") realm: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") clientId: String,
        @Field("refresh_token") refreshToken: String
    ): TokenResponse

    @FormUrlEncoded
    @POST("realms/{realm}/protocol/openid-connect/logout")
    suspend fun logout(
        @Path("realm") realm: String,
        @Field("client_id") clientId: String,
        @Field("refresh_token") refreshToken: String
    ): Response<Unit>
}

