package com.farm.seedadviosory.data.model;

/**
 * Comprehensive soybean variety model with attributes from the ICAR/ICAR-IISR dataset.
 * Inherits all agricultural attributes from MaizeVariety to allow seamless reuse
 * across recommendation result cards, detail screens, and evidence views.
 */
public class SoybeanVariety extends MaizeVariety {

    public SoybeanVariety() {
        super();
        setCrop("Soybean");
    }
}
