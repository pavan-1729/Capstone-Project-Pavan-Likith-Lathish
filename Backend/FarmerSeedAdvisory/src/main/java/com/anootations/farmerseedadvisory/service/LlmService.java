package com.anootations.farmerseedadvisory.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Gateway to LLM services (Google Gemini API with model fallback, and local Ollama fallback).
 *
 * All LLM communication happens server-side.
 * Configuration (application.properties / maize-seed-rag/.env):
 *   llm.provider=gemini
 *   gemini.api.key=YOUR_GEMINI_API_KEY
 *   gemini.model=gemini-3.6-flash
 *   gemini.fallback-models=gemini-3.8-flash,gemini-3.7-flash,gemini-3.6-flash,gemini-2.5-flash,gemini-2.0-flash,gemini-1.5-flash,gemini-2.5-flash-lite
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    @Value("${llm.provider:gemini}")
    private String llmProvider;

    // Google Gemini API configuration
    @Value("${gemini.api.key:${GEMINI_API_KEY:YOUR_GEMINI_API_KEY}}")
    private String geminiApiKey;

    @Value("${gemini.model:${GEMINI_MODEL:gemini-3.6-flash}}")
    private String geminiModel;

    @Value("${gemini.fallback-models:gemini-3.8-flash,gemini-3.7-flash,gemini-3.6-flash,gemini-2.5-flash,gemini-2.0-flash,gemini-1.5-flash,gemini-2.5-flash-lite}")
    private String geminiFallbackModels;

    @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta/models/}")
    private String geminiBaseUrl;

    @Value("${gemini.temperature:0.2}")
    private double geminiTemperature;

    // Local Ollama configuration
    @Value("${llm.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${llm.model:llama3}")
    private String ollamaModel;

    @Value("${llm.timeout-seconds:120}")
    private int timeoutSeconds;

    @Value("${llm.api-path:/api/generate}")
    private String ollamaApiPath;

    private final Gson gson = new Gson();
    private String activeModelUsed;

    // ----------------------------------------------------------------
    // Public API: send a prompt, get text response
    // ----------------------------------------------------------------

    /**
     * Send a prompt to the LLM and return the raw text response.
     * Tries configured provider (default: Google Gemini), then falls back to Ollama.
     */
    public String generate(String prompt) {
        if ("gemini".equalsIgnoreCase(llmProvider)) {
            String geminiResponse = generateWithGemini(prompt);
            if (geminiResponse != null && !geminiResponse.isBlank()) {
                return geminiResponse;
            }
            log.warn("Gemini generation failed; checking local Ollama server...");
        }

        // Try Ollama
        String ollamaResponse = generateWithOllama(prompt);
        if (ollamaResponse != null && !ollamaResponse.isBlank()) {
            return ollamaResponse;
        }

        return null;
    }

    /**
     * Check if an LLM is available (Gemini API key configured or local Ollama up).
     */
    public boolean isAvailable() {
        if ("gemini".equalsIgnoreCase(llmProvider)) {
            if (geminiApiKey != null && !geminiApiKey.isBlank() && !geminiApiKey.contains("YOUR_API_KEY")) {
                return true;
            }
        }
        return isOllamaAvailable();
    }

    /**
     * Returns the name of the model that successfully generated the last response.
     */
    public String getActiveModel() {
        if (activeModelUsed != null && !activeModelUsed.isBlank()) {
            return activeModelUsed;
        }
        return "gemini".equalsIgnoreCase(llmProvider) ? geminiModel : ollamaModel;
    }

    // ----------------------------------------------------------------
    // Google Gemini API implementation with model fallback
    // ----------------------------------------------------------------

    private String generateWithGemini(String prompt) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("Gemini API key is not configured.");
            return null;
        }

        List<String> modelsToTry = buildGeminiModelList();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        for (String model : modelsToTry) {
            String result = callGeminiModel(client, model, prompt);
            if (result != null && !result.isBlank()) {
                activeModelUsed = model;
                log.info("Gemini recommendation successfully generated using model: {}", model);
                return result;
            }
        }

        log.error("All configured Gemini models failed.");
        return null;
    }

    private String callGeminiModel(OkHttpClient client, String model, String prompt) {
        String cleanBaseUrl = geminiBaseUrl.endsWith("/") ? geminiBaseUrl : geminiBaseUrl + "/";
        String url = cleanBaseUrl + model + ":generateContent?key=" + geminiApiKey;

        try {
            // Build Gemini request body
            JsonObject root = new JsonObject();

            JsonArray contents = new JsonArray();
            JsonObject contentObj = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject partObj = new JsonObject();
            partObj.addProperty("text", prompt);
            parts.add(partObj);
            contentObj.add("parts", parts);
            contents.add(contentObj);
            root.add("contents", contents);

            JsonObject genConfig = new JsonObject();
            genConfig.addProperty("temperature", geminiTemperature);
            genConfig.addProperty("responseMimeType", "application/json");
            root.add("generationConfig", genConfig);

            RequestBody body = RequestBody.create(
                    gson.toJson(root),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            log.info("Calling Google Gemini API model: {}", model);

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    log.warn("Gemini model {} returned HTTP {}: {}", model, response.code(),
                            errBody.substring(0, Math.min(250, errBody.length())));
                    return null;
                }

                if (response.body() == null) {
                    log.warn("Gemini model {} returned empty response body", model);
                    return null;
                }

                String responseStr = response.body().string();
                JsonObject resJson = JsonParser.parseString(responseStr).getAsJsonObject();

                if (resJson.has("candidates")) {
                    JsonArray candidates = resJson.getAsJsonArray("candidates");
                    if (candidates.size() > 0) {
                        JsonObject cand = candidates.get(0).getAsJsonObject();
                        if (cand.has("content")) {
                            JsonObject content = cand.getAsJsonObject("content");
                            if (content.has("parts")) {
                                JsonArray resParts = content.getAsJsonArray("parts");
                                if (resParts.size() > 0) {
                                    return resParts.get(0).getAsJsonObject().get("text").getAsString().trim();
                                }
                            }
                        }
                    }
                }

                log.warn("Gemini response missing candidate parts: {}",
                        responseStr.substring(0, Math.min(200, responseStr.length())));
                return null;
            }

        } catch (IOException e) {
            log.warn("Gemini API connection error for model {}: {}", model, e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Unexpected error calling Gemini model {}: {}", model, e.getMessage());
            return null;
        }
    }

    private List<String> buildGeminiModelList() {
        Set<String> set = new LinkedHashSet<>();
        if (geminiModel != null && !geminiModel.isBlank()) {
            set.add(geminiModel.trim());
        }
        if (geminiFallbackModels != null && !geminiFallbackModels.isBlank()) {
            String[] fallbacks = geminiFallbackModels.split(",");
            for (String f : fallbacks) {
                if (!f.isBlank()) set.add(f.trim());
            }
        }
        return new ArrayList<>(set);
    }

    // ----------------------------------------------------------------
    // Local Ollama server implementation
    // ----------------------------------------------------------------

    private String generateWithOllama(String prompt) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        String url = ollamaBaseUrl + ollamaApiPath;

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", ollamaModel);
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("stream", false);

        RequestBody body = RequestBody.create(
                gson.toJson(requestBody),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        log.info("Sending prompt to local Ollama at {} (model: {})", url, ollamaModel);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("Ollama returned HTTP {}", response.code());
                return null;
            }

            String responseBody = response.body().string();
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();

            if (responseJson.has("response")) {
                String llmResponse = responseJson.get("response").getAsString().trim();
                activeModelUsed = ollamaModel;
                log.info("Ollama response received ({} chars)", llmResponse.length());
                return llmResponse;
            }

            log.warn("Ollama response missing 'response' field: {}",
                    responseBody.substring(0, Math.min(200, responseBody.length())));
            return null;

        } catch (IOException e) {
            log.warn("Failed to call Ollama: {}", e.getMessage());
            return null;
        }
    }

    private boolean isOllamaAvailable() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        String healthUrl = ollamaBaseUrl + "/api/version";
        Request request = new Request.Builder().url(healthUrl).get().build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            return false;
        }
    }
}
