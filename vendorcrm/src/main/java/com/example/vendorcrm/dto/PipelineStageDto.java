package com.example.vendorcrm.dto;

/** key: newOrders | packedReady | inTransit | delivered */
public class PipelineStageDto {

    private String key;
    private String stage;
    private long count;
    private String status;
    private String color;

    public PipelineStageDto() {
    }

    public PipelineStageDto(
            String key,
            String stage,
            long count,
            String status,
            String color
    ) {
        this.key = key;
        this.stage = stage;
        this.count = count;
        this.status = status;
        this.color = color;
    }

    public String getKey() {
        return key;
    }

    public String getStage() {
        return stage;
    }

    public long getCount() {
        return count;
    }

    public String getStatus() {
        return status;
    }

    public String getColor() {
        return color;
    }
}