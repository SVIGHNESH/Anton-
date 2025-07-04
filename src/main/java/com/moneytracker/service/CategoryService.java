package com.moneytracker.service;

import com.moneytracker.database.DatabaseManager;
import com.moneytracker.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing categories in the money tracker application.
 */
public class CategoryService {
    
    private final DatabaseManager databaseManager;
    
    public CategoryService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * Get all categories
     */
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY is_default DESC, name ASC";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        }
        
        return categories;
    }
    
    /**
     * Get category by ID
     */
    public Optional<Category> getCategoryById(Long id) throws SQLException {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Create a new category
     */
    public Category createCategory(String name, String description, String color) throws SQLException {
        Category category = new Category(name, description, color);
        
        String sql = "INSERT INTO categories (name, description, color, is_default) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getColor());
            stmt.setBoolean(4, category.isDefault());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
        
        return category;
    }
    
    /**
     * Update a category
     */
    public void updateCategory(Long categoryId, String name, String description, String color) throws SQLException {
        String sql = "UPDATE categories SET name = ?, description = ?, color = ? WHERE id = ? AND is_default = 0";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, color);
            stmt.setLong(4, categoryId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete a category (only non-default categories)
     */
    public void deleteCategory(Long categoryId) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ? AND is_default = 0";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, categoryId);
            stmt.executeUpdate();
        }
    }
    
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setColor(rs.getString("color"));
        category.setIcon(rs.getString("icon"));
        category.setDefault(rs.getBoolean("is_default"));
        return category;
    }
}
