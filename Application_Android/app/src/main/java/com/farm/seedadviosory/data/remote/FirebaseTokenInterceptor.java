package com.farm.seedadviosory.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp interceptor that automatically attaches the Firebase ID token
 * to every authenticated request.
 *
 * Usage: Retrofit calls do NOT need to manually pass an Authorization header.
 * The interceptor fetches a fresh (or cached) token and injects it.
 *
 * If no Firebase user is signed in, the request is sent without Authorization.
 */
public class FirebaseTokenInterceptor implements Interceptor {

    private static final String TAG = "FirebaseTokenInterceptor";
    private static final long TOKEN_FETCH_TIMEOUT_MS = 5000;

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();

        // Get current Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Not authenticated — send without token
            return chain.proceed(original);
        }

        // Synchronously fetch the Firebase ID token
        String token = getFirebaseToken(user);

        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Firebase token unavailable — sending request without Authorization");
            return chain.proceed(original);
        }

        // Attach Bearer token
        Request authenticated = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(authenticated);
    }

    /**
     * Synchronously retrieves the Firebase ID token.
     * Uses a CountDownLatch to wait for the async callback.
     */
    private String getFirebaseToken(FirebaseUser user) {
        AtomicReference<String> tokenRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        user.getIdToken(false)
                .addOnSuccessListener(result -> {
                    tokenRef.set(result.getToken());
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch Firebase ID token: " + e.getMessage());
                    latch.countDown();
                });

        try {
            latch.await(TOKEN_FETCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Token fetch interrupted");
        }

        return tokenRef.get();
    }
}
