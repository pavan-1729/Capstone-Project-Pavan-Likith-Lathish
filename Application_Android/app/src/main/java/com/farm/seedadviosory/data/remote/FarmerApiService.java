package com.farm.seedadviosory.data.remote;

import com.farm.seedadviosory.data.model.Farmer;
import com.farm.seedadviosory.data.model.FarmerRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * @deprecated Retained for backward compatibility only.
 * Use {@link ApiService} via {@link RetrofitClient#getApiService()} instead.
 *
 * The FirebaseTokenInterceptor in RetrofitClient automatically handles
 * Authorization headers — do not pass them manually.
 */
@Deprecated
public interface FarmerApiService {

    @POST("api/farmer/register")
    Call<Void> registerFarmer(
            @Header("Authorization") String authorization,
            @Body FarmerRequest farmerRequest
    );

    @GET("api/farmer/profile")
    Call<Farmer> getFarmerProfile(
            @Header("Authorization") String authorization
    );
}
