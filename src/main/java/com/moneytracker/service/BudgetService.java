package com.moneytracker.service;

import com.moneytracker.database.DatabaseManager;
import com.moneytracker.model.Budget;
import com.moneytracker.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing budgets in the money tracker application.
 * Provides business logic for budget creation, updates, and calculations.
 */
public class BudgetService {
    
    private final DatabaseManager databaseManager;
    
    public BudgetService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * Create a new budget
     */
    public Budget createBudget(BigDecimal totalAmount, LocalDate startDate, LocalDate endDate, String description) throws SQLException {
        // First, mark any existing active budgets as completed
        completeActiveBudgets();
        
        Budget budget = new Budget(totalAmount, startDate, endDate, description);
        budget.calculateInitialDailyBudget();
        
        String sql = """
            INSERT INTO budgets (total_amount, spent_amount, daily_budget, start_date, end_date, 
                               status, description, last_daily_budget_update) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setBigDecimal(1, budget.getTotalAmount());
            stmt.setBigDecimal(2, budget.getSpentAmount());
            stmt.setBigDecimal(3, budget.getDailyBudget());
            stmt.setDate(4, Date.valueOf(budget.getStartDate()));
            stmt.setDate(5, Date.valueOf(budget.getEndDate()));
            stmt.setString(6, budget.getStatus().name());
            stmt.setString(7, budget.getDescription());
            stmt.setDate(8, budget.getLastDailyBudgetUpdate() != null ? Date.valueOf(budget.getLastDailyBudgetUpdate()) : null);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating budget failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    budget.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating budget failed, no ID obtained.");
                }
            }
        }
        
        // Create initial budget transaction
        insertBudgetTransaction(budget);
        
        return budget;
    }
    
    /**
     * Get the current active budget
     */
    public Optional<Budget> getCurrentBudget() throws SQLException {
        String sql = "SELECT * FROM budgets WHERE status = 'ACTIVE' ORDER BY created_at DESC LIMIT 1";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return Optional.of(mapResultSetToBudget(rs));
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Update budget spent amount
     */
    public void updateSpentAmount(Long budgetId, BigDecimal newSpentAmount) throws SQLException {
        String sql = "UPDATE budgets SET spent_amount = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setBigDecimal(1, newSpentAmount);
            stmt.setLong(2, budgetId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Recalculate and update daily budget
     */
    public void recalculateDailyBudget(Long budgetId) throws SQLException {
        Optional<Budget> budgetOpt = getBudgetById(budgetId);
        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            budget.recalculateDailyBudget();
            
            String sql = "UPDATE budgets SET daily_budget = ?, last_daily_budget_update = ? WHERE id = ?";
            
            try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
                stmt.setBigDecimal(1, budget.getDailyBudget());
                stmt.setDate(2, Date.valueOf(budget.getLastDailyBudgetUpdate()));
                stmt.setLong(3, budgetId);
                stmt.executeUpdate();
            }
        }
    }
    
    /**
     * Get budget by ID
     */
    public Optional<Budget> getBudgetById(Long id) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBudget(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Get all budgets ordered by creation date (newest first)
     */
    public List<Budget> getAllBudgets() throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budgets ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                budgets.add(mapResultSetToBudget(rs));
            }
        }
        
        return budgets;
    }
    
    /**
     * Complete current active budget and mark as completed
     */
    public void completeCurrentBudget() throws SQLException {
        String sql = "UPDATE budgets SET status = 'COMPLETED', updated_at = CURRENT_TIMESTAMP WHERE status = 'ACTIVE'";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
    
    /**
     * Calculate budget summary statistics
     */
    public BudgetSummary calculateBudgetSummary(Long budgetId) throws SQLException {
        Optional<Budget> budgetOpt = getBudgetById(budgetId);
        if (budgetOpt.isEmpty()) {
            return null;
        }
        
        Budget budget = budgetOpt.get();
        
        // Calculate total expenses for this budget
        String expenseSql = """
            SELECT SUM(amount) as total_expenses, COUNT(*) as transaction_count 
            FROM transactions 
            WHERE budget_id = ? AND type = 'EXPENSE'
            """;
        
        BigDecimal totalExpenses = BigDecimal.ZERO;
        int transactionCount = 0;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(expenseSql)) {
            stmt.setLong(1, budgetId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal expenses = rs.getBigDecimal("total_expenses");
                    totalExpenses = expenses != null ? expenses : BigDecimal.ZERO;
                    transactionCount = rs.getInt("transaction_count");
                }
            }
        }
        
        return new BudgetSummary(budget, totalExpenses, transactionCount);
    }
    
    /**
     * Private helper methods
     */
    
    private void completeActiveBudgets() throws SQLException {
        String sql = "UPDATE budgets SET status = 'COMPLETED' WHERE status = 'ACTIVE'";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
    
    private void insertBudgetTransaction(Budget budget) throws SQLException {
        String sql = """
            INSERT INTO transactions (type, amount, description, budget_id, timestamp) 
            VALUES ('SET_BUDGET', ?, ?, ?, CURRENT_TIMESTAMP)
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setBigDecimal(1, budget.getTotalAmount());
            stmt.setString(2, "Budget set: " + (budget.getDescription() != null ? budget.getDescription() : "New budget"));
            stmt.setLong(3, budget.getId());
            stmt.executeUpdate();
        }
    }
    
    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setId(rs.getLong("id"));
        budget.setTotalAmount(rs.getBigDecimal("total_amount"));
        budget.setSpentAmount(rs.getBigDecimal("spent_amount"));
        budget.setDailyBudget(rs.getBigDecimal("daily_budget"));
        budget.setStartDate(rs.getDate("start_date").toLocalDate());
        budget.setEndDate(rs.getDate("end_date").toLocalDate());
        budget.setStatus(Budget.BudgetStatus.valueOf(rs.getString("status")));
        budget.setDescription(rs.getString("description"));
        
        Date lastUpdate = rs.getDate("last_daily_budget_update");
        if (lastUpdate != null) {
            budget.setLastDailyBudgetUpdate(lastUpdate.toLocalDate());
        }
        
        return budget;
    }
    
    /**
     * Inner class for budget summary data
     */
    public static class BudgetSummary {
        private final Budget budget;
        private final BigDecimal totalExpenses;
        private final int transactionCount;
        
        public BudgetSummary(Budget budget, BigDecimal totalExpenses, int transactionCount) {
            this.budget = budget;
            this.totalExpenses = totalExpenses;
            this.transactionCount = transactionCount;
        }
        
        public Budget getBudget() { return budget; }
        public BigDecimal getTotalExpenses() { return totalExpenses; }
        public int getTransactionCount() { return transactionCount; }
        public BigDecimal getRemainingBudget() { return budget.getTotalAmount().subtract(totalExpenses); }
        public double getSpentPercentage() { 
            if (budget.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) return 0.0;
            return totalExpenses.divide(budget.getTotalAmount(), 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100;
        }
    }
}
