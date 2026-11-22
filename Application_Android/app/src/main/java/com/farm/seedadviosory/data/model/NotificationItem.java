package com.farm.seedadviosory.data.model;

/**
 * Notification item model.
 */
public class NotificationItem {
    private String id;
    private String title;
    private String message;
    private String category; // crop, weather, disease, fertilizer, system
    private String icon;
    private String timestamp;
    private boolean isRead;
    private boolean isDemo;

    public NotificationItem() { }

    public NotificationItem(String id, String title, String message,
                             String category, String icon, String timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.category = category;
        this.icon = icon;
        this.timestamp = timestamp;
        this.isRead = false;
        this.isDemo = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isDemo() { return isDemo; }
    public void setDemo(boolean demo) { isDemo = demo; }
}
