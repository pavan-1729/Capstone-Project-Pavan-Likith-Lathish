package com.farm.seedadviosory.data.model;

/**
 * Crop advisory item model.
 */
public class CropAdvisory {
    private String id;
    private String title;
    private String description;
    private String category;
    private String icon;
    private String date;
    private String priority; // High, Medium, Low

    public CropAdvisory() { }

    public CropAdvisory(String id, String title, String description,
                         String category, String icon, String date, String priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.icon = icon;
        this.date = date;
        this.priority = priority;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
