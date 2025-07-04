package com.moneytracker.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a financial transaction in the money tracker application.
 * This includes expenses, income, and budget adjustments.
 */
public class Transaction {
    
    public enum TransactionType {
        EXPENSE("Expense"),
        INCOME("Income"),
        SET_BUDGET("Set Budget"),
        SET_DAILY_BUDGET("Set Daily Budget");
        
        private final String displayName;
        
        TransactionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String category;
    private LocalDateTime timestamp;
    private String notes;
    
    // Constructors
    public Transaction() {
        this.timestamp = LocalDateTime.now();
    }
    
    public Transaction(TransactionType type, BigDecimal amount, String description) {
        this();
        this.type = type;
        this.amount = amount;
        this.description = description;
    }
    
    public Transaction(TransactionType type, BigDecimal amount, String description, String category) {
        this(type, amount, description);
        this.category = category;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    // Utility methods
    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }
    
    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }
    
    public boolean isBudgetRelated() {
        return type == TransactionType.SET_BUDGET || type == TransactionType.SET_DAILY_BUDGET;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction that = (Transaction) obj;
        return Objects.equals(id, that.id) &&
               type == that.type &&
               Objects.equals(amount, that.amount) &&
               Objects.equals(description, that.description) &&
               Objects.equals(category, that.category) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, type, amount, description, category, timestamp);
    }
    
    @Override
    public String toString() {
        return String.format("Transaction{id=%d, type=%s, amount=%s, description='%s', category='%s', timestamp=%s}",
                id, type, amount, description, category, timestamp);
    }
}
