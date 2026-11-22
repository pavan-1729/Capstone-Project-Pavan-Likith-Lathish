package com.farm.seedadviosory.data.remote;

import com.farm.seedadviosory.data.model.Farmer;
import com.farm.seedadviosory.data.model.FarmerRequest;
import com.farm.seedadviosory.data.model.MaizeRecommendationRequest;
import com.farm.seedadviosory.data.model.RecommendationResponse;
import com.farm.seedadviosory.data.model.WeatherInfo;
import com.farm.seedadviosory.data.model.MaizeVariety;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Single consolidated Retrofit interface for the Farmer-Aware backend.
 *
 * Authorization is handled automatically by FirebaseTokenInterceptor —
 * do NOT pass @Header("Authorization") manually in these methods.
 *
 * All paths match the Spring Boot controller mappings exactly.
 */
public interface ApiService {

    // ----------------------------------------------------------------
    // Farmer APIs
    // ----------------------------------------------------------------

    /** POST /api/farmer/register — register a new farmer profile */
    @POST("api/farmer/register")
    Call<Farmer> registerFarmer(@Body FarmerRequest farmerRequest);

    /** GET /api/farmer/profile — get authenticated farmer's profile */
    @GET("api/farmer/profile")
    Call<Farmer> getFarmerProfile();

    /** PUT /api/farmer/profile — update farmer's profile */
    @PUT("api/farmer/profile")
    Call<Farmer> updateFarmerProfile(@Body FarmerRequest farmerRequest);

    // ----------------------------------------------------------------
    // Weather API
    // ----------------------------------------------------------------

    /** GET /api/weather?state=X&district=Y — get weather for farmer's location */
    @GET("api/weather")
    Call<WeatherInfo> getWeather(
            @Query("state") String state,
            @Query("district") String district
    );

    // ----------------------------------------------------------------
    // Maize Variety APIs
    // ----------------------------------------------------------------

    /** GET /api/maize/varieties/{varietyId} — variety detail */
    @GET("api/maize/varieties/{varietyId}")
    Call<MaizeVariety> getMaizeVariety(@Path("varietyId") String varietyId);

    // ----------------------------------------------------------------
    // Soybean Variety APIs
    // ----------------------------------------------------------------

    /** GET /api/soybean/varieties/{varietyId} — variety detail */
    @GET("api/soybean/varieties/{varietyId}")
    Call<com.farm.seedadviosory.data.model.SoybeanVariety> getSoybeanVariety(@Path("varietyId") String varietyId);

    /** GET /api/soybean/search — search and rank soybean varieties by state, traits, and criteria */
    @GET("api/soybean/search")
    Call<java.util.List<com.farm.seedadviosory.data.model.SoybeanVariety>> searchSoybeanVarieties(
            @Query("query") String query,
            @Query("state") String state,
            @Query("district") String district,
            @Query("season") String season,
            @Query("irrigation") String irrigation,
            @Query("soilType") String soilType,
            @Query("priority") String priority,
            @Query("maxMaturityDays") int maxMaturityDays,
            @Query("limit") int limit
    );

    // ----------------------------------------------------------------
    // Cotton Variety APIs
    // ----------------------------------------------------------------

    /** GET /api/cotton/varieties/{varietyId} — variety detail */
    @GET("api/cotton/varieties/{varietyId}")
    Call<com.farm.seedadviosory.data.model.CottonVariety> getCottonVariety(@Path("varietyId") String varietyId);

    /** GET /api/cotton/search — search and rank cotton varieties by state, traits, and criteria */
    @GET("api/cotton/search")
    Call<java.util.List<com.farm.seedadviosory.data.model.CottonVariety>> searchCottonVarieties(
            @Query("query") String query,
            @Query("state") String state,
            @Query("district") String district,
            @Query("season") String season,
            @Query("irrigation") String irrigation,
            @Query("soilType") String soilType,
            @Query("priority") String priority,
            @Query("maxMaturityDays") int maxMaturityDays,
            @Query("limit") int limit
    );

    // ----------------------------------------------------------------
    // Recommendation APIs
    // ----------------------------------------------------------------

    /** POST /api/recommendations/maize — run RAG recommendation */
    @POST("api/recommendations/maize")
    Call<RecommendationResponse> recommendMaize(@Body MaizeRecommendationRequest request);

    /** POST /api/recommendations/soybean — run soybean RAG recommendation */
    @POST("api/recommendations/soybean")
    Call<RecommendationResponse> recommendSoybean(@Body MaizeRecommendationRequest request);

    /** POST /api/recommendations/cotton — run cotton RAG recommendation */
    @POST("api/recommendations/cotton")
    Call<RecommendationResponse> recommendCotton(@Body MaizeRecommendationRequest request);

    // ----------------------------------------------------------------
    // Public (no auth)
    // ----------------------------------------------------------------

    /** GET /api/public/test — verify server is up */
    @GET("api/public/test")
    Call<String> publicTest();
}
