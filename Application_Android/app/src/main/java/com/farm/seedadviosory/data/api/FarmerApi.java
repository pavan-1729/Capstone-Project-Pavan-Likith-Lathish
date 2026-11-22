package com.farm.seedadviosory.data.api;

import com.farm.seedadviosory.data.model.FarmerRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FarmerApi {

    @POST("api/farmer/register")
    Call<Object> registerFarmer(
            @Header("Authorization") String authorization,
            @Body FarmerRequest farmerRequest
    );
}