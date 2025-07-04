package com.moneytracker.model;

import java.util.Objects;

/**
 * Represents a spending category in the money tracker application.
 * Categories help organize and analyze expenses by type.
 */
public class Category {
    
    private Long id;
    private String name;
    private String description;
    private String color; // Hex color code for UI representation
    private String icon; // Icon identifier for UI
    private boolean isDefault; // Whether this is a default system category
    
    // Default categories that should be available in the application
    public static final String[] DEFAULT_CATEGORIES = {
        "Food & Dining",
        "Transportation",
        "Shopping",
        "Entertainment",
        "Bills & Utilities",
        "Healthcare",
        "Education",
        "Travel",
        "Personal Care",
        "Other"
    };
    
    // Constructors
    public Category() {
        this.isDefault = false;
    }
    
    public Category(String name) {
        this();
        this.name = name;
    }
    
    public Category(String name, String description) {
        this(name);
        this.description = description;
    }
    
    public Category(String name, String description, String color) {
        this(name, description);
        this.color = color;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    // Utility methods
    
    /**
     * Get display color or default if not set
     */
    public String getDisplayColor() {
        return color != null ? color : "#4A90E2"; // Default blue color
    }
    
    /**
     * Check if this category can be deleted (non-default categories only)
     */
    public boolean canDelete() {
        return !isDefault;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return Objects.equals(id, category.id) &&
               Objects.equals(name, category.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
    
    @Override
    public String toString() {
        return name != null ? name : "Unnamed Category";
    }
}
