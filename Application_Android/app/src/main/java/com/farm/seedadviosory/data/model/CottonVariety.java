package com.farm.seedadviosory.data.model;

/**
 * Comprehensive cotton variety model with attributes from the ICAR/ICAR-CICR dataset.
 * Inherits all agricultural attributes from MaizeVariety to allow seamless reuse
 * across recommendation result cards, detail screens, and evidence views.
 */
public class CottonVariety extends MaizeVariety {

    public CottonVariety() {
        super();
        setCrop("Cotton");
    }
}
