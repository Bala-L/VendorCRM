package com.example.vendorcrm.dto;

import java.util.List;

public class CommandCentreResponse {

    private String userRole;
    private String vendorName;

    private List<KpiDto> liveMetrics;
    private List<ActionItemDto> actionRequiredItems;
    private List<PipelineStageDto> orderPipeline;
    private List<CarrierMonitoringDto> deliveryMonitoring;
    private SettlementHealthDto settlementHealth;
    private List<InventoryHealthDto> inventory;
    private List<ReturnMonitoringDto> returns;
    private List<OperationalOrderDto> orders;
    private List<ActivityFeedDto> activityFeed;

    public CommandCentreResponse() {}

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public List<KpiDto> getLiveMetrics() {
        return liveMetrics;
    }

    public void setLiveMetrics(List<KpiDto> liveMetrics) {
        this.liveMetrics = liveMetrics;
    }

    public List<ActionItemDto> getActionRequiredItems() {
        return actionRequiredItems;
    }

    public void setActionRequiredItems(List<ActionItemDto> actionRequiredItems) {
        this.actionRequiredItems = actionRequiredItems;
    }

    public List<PipelineStageDto> getOrderPipeline() {
        return orderPipeline;
    }

    public void setOrderPipeline(List<PipelineStageDto> orderPipeline) {
        this.orderPipeline = orderPipeline;
    }

    public List<CarrierMonitoringDto> getDeliveryMonitoring() {
        return deliveryMonitoring;
    }

    public void setDeliveryMonitoring(List<CarrierMonitoringDto> deliveryMonitoring) {
        this.deliveryMonitoring = deliveryMonitoring;
    }

    public SettlementHealthDto getSettlementHealth() {
        return settlementHealth;
    }

    public void setSettlementHealth(SettlementHealthDto settlementHealth) {
        this.settlementHealth = settlementHealth;
    }

    public List<InventoryHealthDto> getInventory() {
        return inventory;
    }

    public void setInventory(List<InventoryHealthDto> inventory) {
        this.inventory = inventory;
    }

    public List<ReturnMonitoringDto> getReturns() {
        return returns;
    }

    public void setReturns(List<ReturnMonitoringDto> returns) {
        this.returns = returns;
    }

    public List<OperationalOrderDto> getOrders() {
        return orders;
    }

    public void setOrders(List<OperationalOrderDto> orders) {
        this.orders = orders;
    }

    public List<ActivityFeedDto> getActivityFeed() {
        return activityFeed;
    }

    public void setActivityFeed(List<ActivityFeedDto> activityFeed) {
        this.activityFeed = activityFeed;
    }
}