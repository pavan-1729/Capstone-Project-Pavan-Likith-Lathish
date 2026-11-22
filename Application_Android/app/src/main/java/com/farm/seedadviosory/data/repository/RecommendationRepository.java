package com.farm.seedadviosory.data.repository;

import com.farm.seedadviosory.data.model.MaizeRecommendationRequest;
import com.farm.seedadviosory.data.model.RecommendationResponse;
import com.farm.seedadviosory.data.remote.ApiService;
import com.farm.seedadviosory.data.remote.RetrofitClient;

import retrofit2.Call;

/**
 * Repository for maize recommendation operations.
 * Always uses the real Spring Boot RAG backend.
 */
public class RecommendationRepository {

    private final ApiService apiService;

    public RecommendationRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    /**
     * Returns a Retrofit Call for the RAG recommendation endpoint.
     * Routes to Maize, Soybean, or Cotton RAG based on the requested crop.
     */
    public Call<RecommendationResponse> getRecommendations(MaizeRecommendationRequest request) {
        if (request != null && "soybean".equalsIgnoreCase(request.getCrop())) {
            return apiService.recommendSoybean(request);
        }
        if (request != null && "cotton".equalsIgnoreCase(request.getCrop())) {
            return apiService.recommendCotton(request);
        }
        return apiService.recommendMaize(request);
    }

    /**
     * Explicit Call for the Soybean RAG recommendation endpoint.
     */
    public Call<RecommendationResponse> getSoybeanRecommendations(MaizeRecommendationRequest request) {
        return apiService.recommendSoybean(request);
    }

    /**
     * Explicit Call for the Cotton RAG recommendation endpoint.
     */
    public Call<RecommendationResponse> getCottonRecommendations(MaizeRecommendationRequest request) {
        return apiService.recommendCotton(request);
    }

    /**
     * Search soybean varieties with multidimensional agronomic ranking.
     */
    public Call<java.util.List<com.farm.seedadviosory.data.model.SoybeanVariety>> searchSoybeanVarieties(
            String query, String state, String district, String season, String irrigation,
            String soilType, String priority, int maxMaturityDays, int limit) {
        return apiService.searchSoybeanVarieties(query, state, district, season, irrigation, soilType, priority, maxMaturityDays, limit);
    }

    /**
     * Search cotton varieties with multidimensional agronomic ranking.
     */
    public Call<java.util.List<com.farm.seedadviosory.data.model.CottonVariety>> searchCottonVarieties(
            String query, String state, String district, String season, String irrigation,
            String soilType, String priority, int maxMaturityDays, int limit) {
        return apiService.searchCottonVarieties(query, state, district, season, irrigation, soilType, priority, maxMaturityDays, limit);
    }
}
