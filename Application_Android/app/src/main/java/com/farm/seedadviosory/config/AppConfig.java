package com.farm.seedadviosory.config;

/**
 * Central application configuration.
 *
 * BASE_URL controls where the Android app connects.
 *
 * For LAN testing (physical device on the same WiFi):
 *   Replace 10.0.2.2 with your computer's local IP address.
 *   Example: http://192.168.1.105:8080/
 *
 * For emulator (AVD):
 *   Use http://10.0.2.2:8080/ — this maps to localhost on the host machine.
 *
 * For production:
 *   Use your HTTPS backend URL.
 *
 * DEMO_MODE:
 *   false = production mode (real backend only)
 *   true  = allow demo mode for testing
 */
public final class AppConfig {

    private AppConfig() {}

    /**
     * When true, app uses mock data. Set to false for production.
     * The demo login button in LoginActivity is only visible when this is true.
     */
    public static final boolean DEMO_MODE = false;

    // ---------------------------------------------------------------
    // Backend URL — change this for your deployment environment.
    // ---------------------------------------------------------------

    /**
     * Spring Boot backend base URL.
     *
     * Emulator:       http://10.0.2.2:8080/
     * Physical device: http://<your-computer-LAN-IP>:8080/
     * Production:      https://your-production-server.com/
     */
    public static final String BASE_URL = "http://10.230.69.130:8080/";

    /** API timeout in seconds */
    public static final int API_TIMEOUT = 60;

    /** App version info */
    public static final String APP_VERSION = "1.0.0";
    public static final String BUILD_TYPE = "Debug";
    
    /** Demo credentials - only used when DEMO_MODE = true */
    public static final String DEMO_MOBILE = "9876543210";
    public static final String DEMO_PASSWORD = "demo123";
    public static final String DEMO_OTP = "123456";
}
