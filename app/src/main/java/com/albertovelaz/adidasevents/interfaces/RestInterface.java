package com.albertovelaz.adidasevents.interfaces;

import com.albertovelaz.adidasevents.models.AdidasEvent;
import com.albertovelaz.adidasevents.models.JSONModels.*;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Alberto Vélaz
 * Created by albertovelazmoliner on 15/10/2016.
 */

public interface RestInterface {

        @FormUrlEncoded
        @POST("runner")
        Call<ApiResponse> sendData(
                @Field("email") String email,
                @Field("firstName") String firstName,
                @Field("lastName") String lastName,
                @Field("birthdate") String date,
                @Field("country") String country
        );

        @GET("data/event")
        Call<AdidasEvent> loadDataEvent(@Query("timestamp") long timestamp);
}
