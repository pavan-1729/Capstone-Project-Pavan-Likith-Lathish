package com.farm.seedadviosory.data.remote;

import com.farm.seedadviosory.config.AppConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Singleton Retrofit client.
 *
 * Automatically attaches Firebase ID tokens via FirebaseTokenInterceptor.
 * All authenticated API calls do NOT need to manually pass Authorization headers.
 *
 * Base URL is configured in AppConfig.BASE_URL.
 */
public class RetrofitClient {

    private static Retrofit retrofit;
    private static ApiService apiService;

    private RetrofitClient() {}

    /**
     * Returns the single shared Retrofit instance.
     */
    public static synchronized Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(AppConfig.BASE_URL)
                    .client(buildOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Returns the single shared ApiService instance.
     * Use this for all API calls.
     */
    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            apiService = getInstance().create(ApiService.class);
        }
        return apiService;
    }

    /**
     * Compatibility method — returns the same ApiService.
     * Retained so existing LoginActivity and RegisterActivity compile without changes.
     * @deprecated Use getApiService() instead.
     */
    @Deprecated
    public static ApiService getFarmerApiService() {
        return getApiService();
    }

    /**
     * Reset the client — used when BASE_URL changes (e.g., switching environments).
     */
    public static synchronized void reset() {
        retrofit = null;
        apiService = null;
    }

    private static OkHttpClient buildOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // Only log body in debug builds
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        return new OkHttpClient.Builder()
                .addInterceptor(new FirebaseTokenInterceptor())
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(AppConfig.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(AppConfig.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }
}
