package com.moneytracker.service;

import com.moneytracker.database.DatabaseManager;
import com.moneytracker.model.Category;
import com.moneytracker.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for managing transactions in the money tracker application.
 * Provides business logic for transaction creation, updates, and analysis.
 */
public class TransactionService {
    
    private final DatabaseManager databaseManager;
    
    public TransactionService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * Add a new expense transaction
     */
    public Transaction addExpense(BigDecimal amount, String description, Long categoryId, String notes, Long budgetId) throws SQLException {
        Transaction transaction = new Transaction(Transaction.TransactionType.EXPENSE, amount, description);
        transaction.setNotes(notes);
        
        String sql = """
            INSERT INTO transactions (type, amount, description, category_id, notes, timestamp, budget_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, transaction.getType().name());
            stmt.setBigDecimal(2, transaction.getAmount());
            stmt.setString(3, transaction.getDescription());
            stmt.setObject(4, categoryId);
            stmt.setString(5, transaction.getNotes());
            stmt.setTimestamp(6, Timestamp.valueOf(transaction.getTimestamp()));
            stmt.setObject(7, budgetId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
        }
        
        // Update budget spent amount if applicable
        if (budgetId != null) {
            updateBudgetSpentAmount(budgetId);
        }
        
        return transaction;
    }
    
    /**
     * Add income transaction
     */
    public Transaction addIncome(BigDecimal amount, String description, String notes) throws SQLException {
        Transaction transaction = new Transaction(Transaction.TransactionType.INCOME, amount, description);
        transaction.setNotes(notes);
        
        String sql = """
            INSERT INTO transactions (type, amount, description, notes, timestamp) 
            VALUES (?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, transaction.getType().name());
            stmt.setBigDecimal(2, transaction.getAmount());
            stmt.setString(3, transaction.getDescription());
            stmt.setString(4, transaction.getNotes());
            stmt.setTimestamp(5, Timestamp.valueOf(transaction.getTimestamp()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating transaction failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating transaction failed, no ID obtained.");
                }
            }
        }
        
        return transaction;
    }
    
    /**
     * Get all transactions for a specific budget
     */
    public List<Transaction> getTransactionsByBudget(Long budgetId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT t.*, c.name as category_name 
            FROM transactions t 
            LEFT JOIN categories c ON t.category_id = c.id 
            WHERE t.budget_id = ? 
            ORDER BY t.timestamp DESC
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, budgetId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    transaction.setCategory(rs.getString("category_name"));
                    transactions.add(transaction);
                }
            }
        }
        
        return transactions;
    }
    
    /**
     * Get all transactions within a date range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT t.*, c.name as category_name 
            FROM transactions t 
            LEFT JOIN categories c ON t.category_id = c.id 
            WHERE DATE(t.timestamp) BETWEEN ? AND ? 
            ORDER BY t.timestamp DESC
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    transaction.setCategory(rs.getString("category_name"));
                    transactions.add(transaction);
                }
            }
        }
        
        return transactions;
    }
    
    /**
     * Get recent transactions (last 30 days)
     */
    public List<Transaction> getRecentTransactions(int limit) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT t.*, c.name as category_name 
            FROM transactions t 
            LEFT JOIN categories c ON t.category_id = c.id 
            ORDER BY t.timestamp DESC 
            LIMIT ?
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    transaction.setCategory(rs.getString("category_name"));
                    transactions.add(transaction);
                }
            }
        }
        
        return transactions;
    }
    
    /**
     * Delete a transaction
     */
    public void deleteTransaction(Long transactionId) throws SQLException {
        // First get the transaction details for budget update
        Optional<Transaction> transactionOpt = getTransactionById(transactionId);
        
        String sql = "DELETE FROM transactions WHERE id = ?";
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, transactionId);
            stmt.executeUpdate();
        }
        
        // Update budget if it was an expense
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            if (transaction.isExpense()) {
                // Get budget ID for this transaction and update spent amount
                updateBudgetSpentAmountForTransaction(transactionId);
            }
        }
    }
    
    /**
     * Update a transaction
     */
    public void updateTransaction(Long transactionId, BigDecimal amount, String description, Long categoryId, String notes) throws SQLException {
        String sql = """
            UPDATE transactions 
            SET amount = ?, description = ?, category_id = ?, notes = ? 
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setString(2, description);
            stmt.setObject(3, categoryId);
            stmt.setString(4, notes);
            stmt.setLong(5, transactionId);
            stmt.executeUpdate();
        }
        
        // Update budget spent amount
        updateBudgetSpentAmountForTransaction(transactionId);
    }
    
    /**
     * Get transaction by ID
     */
    public Optional<Transaction> getTransactionById(Long id) throws SQLException {
        String sql = """
            SELECT t.*, c.name as category_name 
            FROM transactions t 
            LEFT JOIN categories c ON t.category_id = c.id 
            WHERE t.id = ?
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    transaction.setCategory(rs.getString("category_name"));
                    return Optional.of(transaction);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Get spending by category for a specific budget
     */
    public Map<String, BigDecimal> getSpendingByCategory(Long budgetId) throws SQLException {
        Map<String, BigDecimal> spendingByCategory = new HashMap<>();
        String sql = """
            SELECT c.name, SUM(t.amount) as total_amount 
            FROM transactions t 
            LEFT JOIN categories c ON t.category_id = c.id 
            WHERE t.budget_id = ? AND t.type = 'EXPENSE' 
            GROUP BY c.name 
            ORDER BY total_amount DESC
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, budgetId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String categoryName = rs.getString("name");
                    if (categoryName == null) categoryName = "Uncategorized";
                    BigDecimal amount = rs.getBigDecimal("total_amount");
                    spendingByCategory.put(categoryName, amount);
                }
            }
        }
        
        return spendingByCategory;
    }
    
    /**
     * Get daily spending for the current month
     */
    public Map<LocalDate, BigDecimal> getDailySpending(LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<LocalDate, BigDecimal> dailySpending = new HashMap<>();
        String sql = """
            SELECT DATE(timestamp) as spending_date, SUM(amount) as daily_total 
            FROM transactions 
            WHERE type = 'EXPENSE' AND DATE(timestamp) BETWEEN ? AND ? 
            GROUP BY DATE(timestamp) 
            ORDER BY spending_date
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("spending_date").toLocalDate();
                    BigDecimal amount = rs.getBigDecimal("daily_total");
                    dailySpending.put(date, amount);
                }
            }
        }
        
        return dailySpending;
    }
    
    /**
     * Private helper methods
     */
    
    private void updateBudgetSpentAmount(Long budgetId) throws SQLException {
        String sql = """
            UPDATE budgets 
            SET spent_amount = (
                SELECT COALESCE(SUM(amount), 0) 
                FROM transactions 
                WHERE budget_id = ? AND type = 'EXPENSE'
            ) 
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, budgetId);
            stmt.setLong(2, budgetId);
            stmt.executeUpdate();
        }
    }
    
    private void updateBudgetSpentAmountForTransaction(Long transactionId) throws SQLException {
        String sql = """
            UPDATE budgets 
            SET spent_amount = (
                SELECT COALESCE(SUM(amount), 0) 
                FROM transactions 
                WHERE budget_id = budgets.id AND type = 'EXPENSE'
            ) 
            WHERE id = (
                SELECT budget_id FROM transactions WHERE id = ?
            )
            """;
        
        try (PreparedStatement stmt = databaseManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, transactionId);
            stmt.executeUpdate();
        }
    }
    
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setType(Transaction.TransactionType.valueOf(rs.getString("type")));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setDescription(rs.getString("description"));
        transaction.setNotes(rs.getString("notes"));
        
        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            transaction.setTimestamp(timestamp.toLocalDateTime());
        }
        
        return transaction;
    }
}
