package com.farm.seedadviosory.data.model;

/**
 * Crop calendar stage model.
 */
public class CropCalendarStage {
    private int dayNumber;
    private String stageName;
    private String description;
    private String icon;
    private boolean isCurrentStage;

    public CropCalendarStage() { }

    public CropCalendarStage(int dayNumber, String stageName, String description,
                              String icon, boolean isCurrentStage) {
        this.dayNumber = dayNumber;
        this.stageName = stageName;
        this.description = description;
        this.icon = icon;
        this.isCurrentStage = isCurrentStage;
    }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public boolean isCurrentStage() { return isCurrentStage; }
    public void setCurrentStage(boolean currentStage) { isCurrentStage = currentStage; }
}
