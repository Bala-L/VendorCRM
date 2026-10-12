package com.example.vendorcrm.dto;

import java.time.Instant;

public class ActivityFeedDto {

    private Long id;
    private String type;
    private String text;
    private Instant at;
    private String time;
    private String icon;
    private String color;

    public ActivityFeedDto() {
    }

    public ActivityFeedDto(
            Long id,
            String type,
            String text,
            Instant at,
            String time,
            String icon,
            String color
    ) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.at = at;
        this.time = time;
        this.icon = icon;
        this.color = color;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Instant getAt() {
        return at;
    }

    public String getTime() {
        return time;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }
}