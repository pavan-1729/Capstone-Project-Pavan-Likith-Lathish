package com.farm.seedadviosory.data.repository;

import com.farm.seedadviosory.config.AppConfig;
import com.farm.seedadviosory.data.model.Farmer;

/**
 * Repository for authentication operations.
 * Uses demo authentication when DEMO_MODE is true.
 * Will use Spring Security + JWT when backend is ready.
 */
public class AuthRepository {

    /**
     * Attempt login with provided credentials.
     * @return Farmer object if successful, null otherwise
     */
    public Farmer login(String mobileOrEmail, String password) {
        if (AppConfig.DEMO_MODE) {
            return demoLogin(mobileOrEmail, password);
        }
        // Future: Use RetrofitClient for real API call
        return null;
    }

    /**
     * Demo login - accepts demo credentials or demo farmer shortcut.
     */
    private Farmer demoLogin(String mobileOrEmail, String password) {
        if ((AppConfig.DEMO_MOBILE.equals(mobileOrEmail) && AppConfig.DEMO_PASSWORD.equals(password))
                || "demo".equals(mobileOrEmail)) {
            return createDemoFarmer();
        }
        return null;
    }

    /**
     * Continue as demo farmer (no credentials needed).
     */
    public Farmer loginAsDemoFarmer() {
        return createDemoFarmer();
    }

    /**
     * Demo registration - always succeeds in demo mode.
     */
    public boolean register(String name, String mobile, String email,
                            String password, String state, String district,
                            String village, String language, String crop) {
        if (AppConfig.DEMO_MODE) {
            return true;
        }
        // Future: Use RetrofitClient for real API call
        return false;
    }

    /**
     * Send OTP for password reset.
     */
    public boolean sendOtp(String mobileOrEmail) {
        if (AppConfig.DEMO_MODE) {
            return true; // Always succeeds in demo mode
        }
        return false;
    }

    /**
     * Verify OTP.
     */
    public boolean verifyOtp(String otp) {
        if (AppConfig.DEMO_MODE) {
            return AppConfig.DEMO_OTP.equals(otp);
        }
        return false;
    }

    /**
     * Reset password.
     */
    public boolean resetPassword(String newPassword) {
        if (AppConfig.DEMO_MODE) {
            return true;
        }
        return false;
    }

    private Farmer createDemoFarmer() {
        Farmer farmer = new Farmer();
        farmer.setName("Demo Farmer");
        farmer.setMobile("9876543210");
        farmer.setEmail("demo@farmeraware.com");
        farmer.setState("Andhra Pradesh");
        farmer.setDistrict("Kurnool");
        farmer.setVillage("");
        farmer.setPreferredLanguage("en");
        farmer.setPrimaryCrop("Maize");
        return farmer;
    }
}
