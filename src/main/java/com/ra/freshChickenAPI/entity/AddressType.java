package com.ra.freshChickenAPI.entity;

public enum AddressType {
    HOME("Home", "🏠"),
    WORK("Work", "💼"),
    OTHER("Other", "📍");
    
    private final String displayName;
    private final String icon;
    
    AddressType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getDisplayNameWithIcon() {
        return icon + " " + displayName;
    }
}