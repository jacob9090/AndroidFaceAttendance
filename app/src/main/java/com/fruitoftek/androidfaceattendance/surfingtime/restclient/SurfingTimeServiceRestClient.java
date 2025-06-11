package com.fruitoftek.androidfaceattendance.surfingtime.restclient;

import java.util.List;

import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiAttLog;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiBioPhoto;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiCommand;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiCommandUpdate;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiInfoRequest;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiInfoResponse;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiUser;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.TokenRequest;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.TokenResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SurfingTimeServiceRestClient {

    @POST("api/auth")
    Call<TokenResponse> requestToken(@Body TokenRequest tokenRequest);

    @POST("api/attendance/device/info")
    Call<ApiInfoResponse> info(@Header("Authorization") String authorization, @Body ApiInfoRequest apiInfoRequest);

    @GET("api/attendance/device/commands")
    Call<List<ApiCommand>> getCommands(@Header("Authorization") String authorization);

    @PUT("api/attendance/device/commands")
    Call<Void> updateCommands(@Header("Authorization") String authorization, @Body List<ApiCommandUpdate> updates);

    @PUT("api/attendance/attlogs")
    Call<Void> upsertAttLogs(@Header("Authorization") String authorization, @Body List<ApiAttLog> apiAttLogs);

    @GET("api/attendance/user/{id}")
    Call<ApiUser> getUserById(@Header("Authorization") String authorization, @Path("id") int id);

    @PUT("api/attendance/user/{id}")
    Call<Void> upsertUser(@Header("Authorization") String authorization, @Path("id") int id, @Body ApiUser user);

    @GET("api/attendance/user/{id}/biophoto")
    Call<ApiBioPhoto> getBioPhotoForUser(@Header("Authorization") String authorization, @Path("id") int id);

    @PUT("api/attendance/user/{id}/biophoto")
    Call<Void> upsertBioPhotoForUser(@Header("Authorization") String authorization, @Path("id") int id, @Body ApiBioPhoto apiBioPhoto);
}
