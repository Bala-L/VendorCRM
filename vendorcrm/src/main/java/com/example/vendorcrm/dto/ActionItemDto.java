package com.example.vendorcrm.dto;

import java.time.Instant;

/** type: CRITICAL | WARNING | ATTENTION. at may be null. */
public class ActionItemDto {

    private String id;
    private String type;
    private String title;
    private String desc;
    private Instant at;
    private String targetTab;
    private String targetSection;
    private String buttonText;

    public ActionItemDto() {
    }

    public ActionItemDto(
            String id,
            String type,
            String title,
            String desc,
            Instant at,
            String targetTab,
            String targetSection,
            String buttonText
    ) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.desc = desc;
        this.at = at;
        this.targetTab = targetTab;
        this.targetSection = targetSection;
        this.buttonText = buttonText;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public Instant getAt() {
        return at;
    }

    public String getTargetTab() {
        return targetTab;
    }

    public String getTargetSection() {
        return targetSection;
    }

    public String getButtonText() {
        return buttonText;
    }
}