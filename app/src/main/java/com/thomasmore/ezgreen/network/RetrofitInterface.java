package com.thomasmore.ezgreen.network;

import com.thomasmore.ezgreen.model.Plant;
import com.thomasmore.ezgreen.model.Response;
import com.thomasmore.ezgreen.model.User;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

public interface RetrofitInterface {

    @POST("users")
    Observable<Response> register(@Body User user);

    @POST("authenticate")
    Observable<Response> login();

    @GET("users/{email}")
    Observable<User> getProfile(@Path("email") String email);

    @PUT("users/{email}")
    Observable<Response> changePassword(@Path("email") String email, @Body User user);

    @POST("users/{email}/password")
    Observable<Response> resetPasswordInit(@Path("email") String email);

    @POST("users/{email}/password")
    Observable<Response> resetPasswordFinish(@Path("email") String email, @Body User user);

    @GET("modules/user/{id}")
    Observable<List<Plant>> getSensorData(@Path("id") String id);

    @PUT("android/profileimage")
    Observable<Response> setProfileImgUrl(@Body User user);

    @PUT("android/connect")
    Observable<Response> setUserId(@Body User user);
}
