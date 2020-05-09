package com.example.samsung_project;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface VkRest {
    @GET("method/users.get")
    Call<Model> request(@Query("user_ids") String user_id, @Query("fields") String fields, @Query("access_token") String access_token, @Query("v") String v);
}
