package com.ra.freshChickenAPI.entity;

public enum SaleType {
    FLASH_SALE("Flash Sale", "⚡ Flash Sale"),
    DIWALI_SALE("Diwali Sale", "🪔 Diwali Sale"),
    FESTIVAL_SALE("Festival Sale", "🎉 Festival Sale"),
    REGULAR("Regular", "Regular");
    
    private final String displayName;
    private final String displayNameWithIcon;
    
    SaleType(String displayName, String displayNameWithIcon) {
        this.displayName = displayName;
        this.displayNameWithIcon = displayNameWithIcon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDisplayNameWithIcon() {
        return displayNameWithIcon;
    }
}