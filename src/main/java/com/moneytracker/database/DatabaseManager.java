package com.moneytracker.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database manager for the Money Tracker application.
 * Handles SQLite database connection and initialization.
 */
public class DatabaseManager {
    
    private static final String DATABASE_NAME = "buckwheat_money_tracker.db";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;
    
    private Connection connection;
    
    /**
     * Initialize the database connection and create tables if they don't exist
     */
    public void initializeDatabase() throws SQLException {
        // Create database file if it doesn't exist
        createDatabaseFile();
        
        // Establish connection
        connection = DriverManager.getConnection(DATABASE_URL);
        
        // Enable foreign keys
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        
        // Create tables
        createTables();
        
        System.out.println("Database initialized successfully: " + DATABASE_NAME);
    }
    
    /**
     * Create database file if it doesn't exist
     */
    private void createDatabaseFile() {
        File dbFile = new File(DATABASE_NAME);
        if (!dbFile.exists()) {
            System.out.println("Creating new database file: " + DATABASE_NAME);
        }
    }
    
    /**
     * Create all required tables
     */
    private void createTables() throws SQLException {
        createCategoriesTable();
        createBudgetsTable();
        createTransactionsTable();
        insertDefaultCategories();
    }
    
    /**
     * Create categories table
     */
    private void createCategoriesTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                description TEXT,
                color TEXT,
                icon TEXT,
                is_default BOOLEAN DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create budgets table
     */
    private void createBudgetsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                total_amount DECIMAL(10,2) NOT NULL,
                spent_amount DECIMAL(10,2) DEFAULT 0.00,
                daily_budget DECIMAL(10,2) DEFAULT 0.00,
                start_date DATE NOT NULL,
                end_date DATE NOT NULL,
                status TEXT DEFAULT 'ACTIVE',
                description TEXT,
                last_daily_budget_update DATE,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Create transactions table
     */
    private void createTransactionsTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                description TEXT NOT NULL,
                category_id INTEGER,
                notes TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                budget_id INTEGER,
                FOREIGN KEY (category_id) REFERENCES categories(id),
                FOREIGN KEY (budget_id) REFERENCES budgets(id)
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    /**
     * Insert default categories if they don't exist
     */
    private void insertDefaultCategories() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM categories WHERE is_default = 1";
        String insertSql = """
            INSERT OR IGNORE INTO categories (name, description, color, is_default) 
            VALUES (?, ?, ?, 1)
            """;
        
        try (Statement checkStmt = connection.createStatement();
             var rs = checkStmt.executeQuery(checkSql)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // No default categories exist, insert them
                try (var insertStmt = connection.prepareStatement(insertSql)) {
                    String[][] defaultCategories = {
                        {"Food & Dining", "Restaurants, groceries, and food delivery", "#FF6B6B"},
                        {"Transportation", "Gas, public transport, parking, and vehicle maintenance", "#4ECDC4"},
                        {"Shopping", "Clothing, electronics, and general purchases", "#45B7D1"},
                        {"Entertainment", "Movies, games, and recreational activities", "#96CEB4"},
                        {"Bills & Utilities", "Electricity, water, internet, and other bills", "#FECA57"},
                        {"Healthcare", "Medical expenses, pharmacy, and health insurance", "#FF9FF3"},
                        {"Education", "Books, courses, and educational expenses", "#54A0FF"},
                        {"Travel", "Vacation, business trips, and travel expenses", "#5F27CD"},
                        {"Personal Care", "Grooming, beauty, and personal items", "#00D2D3"},
                        {"Other", "Miscellaneous expenses", "#8395A7"}
                    };
                    
                    for (String[] category : defaultCategories) {
                        insertStmt.setString(1, category[0]);
                        insertStmt.setString(2, category[1]);
                        insertStmt.setString(3, category[2]);
                        insertStmt.executeUpdate();
                    }
                }
                System.out.println("Default categories inserted");
            }
        }
    }
    
    /**
     * Get the database connection
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Close the database connection
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Database connection closed");
        }
    }
    
    /**
     * Check if the database connection is valid
     */
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
