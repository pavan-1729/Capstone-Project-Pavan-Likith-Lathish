package com.anootations.farmerseedadvisory.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "farmers",
    indexes = {
        @Index(name = "idx_farmer_firebase_uid", columnList = "firebase_uid")
    }
)
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Firebase Auth UID — primary identity link */
    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    private String firebaseUid;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 15)
    private String mobile;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String village;

    /** ISO 639-1 language code: en, te, hi, ta */
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;

    /** Maize / Soybean / Cotton */
    @Column(name = "primary_crop", length = 50)
    private String primaryCrop;

    /** Free-text: "2 acres", "5 hectares", etc. */
    @Column(name = "farm_size", length = 50)
    private String farmSize;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    public Farmer() {}

    // Full constructor used by service layer
    public Farmer(
            String firebaseUid,
            String name,
            String mobile,
            String email,
            String state,
            String district,
            String village,
            String preferredLanguage,
            String primaryCrop,
            String farmSize
    ) {
        this.firebaseUid = firebaseUid;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.state = state;
        this.district = district;
        this.village = village;
        this.preferredLanguage = preferredLanguage;
        this.primaryCrop = primaryCrop;
        this.farmSize = farmSize;
    }

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------

    public Long getId() { return id; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getPrimaryCrop() { return primaryCrop; }
    public void setPrimaryCrop(String primaryCrop) { this.primaryCrop = primaryCrop; }

    public String getFarmSize() { return farmSize; }
    public void setFarmSize(String farmSize) { this.farmSize = farmSize; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
