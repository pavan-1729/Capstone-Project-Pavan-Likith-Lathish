package com.anootations.farmerseedadvisory.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/public/test")
    public String publicTest() {

        return "Public API is working";
    }

    @GetMapping("/api/farmer/test")
    public String farmerTest(Authentication authentication) {

        return "Farmer authenticated successfully. UID: "
                + authentication.getName();
    }
}