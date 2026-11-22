package com.anootations.farmerseedadvisory.controller;

import com.anootations.farmerseedadvisory.dto.WeatherResponse;
import com.anootations.farmerseedadvisory.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Weather API — provides real-time weather data to the Android app.
 * The weather provider API key never leaves the server.
 *
 * GET /api/weather?state=Andhra+Pradesh&district=Kurnool
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam String state,
            @RequestParam(required = false) String district,
            Authentication authentication
    ) {
        WeatherResponse response = weatherService.getWeather(state, district);
        return ResponseEntity.ok(response);
    }
}
