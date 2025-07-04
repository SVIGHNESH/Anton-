package com.moneytracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a budget period in the money tracker application.
 * Similar to the budget management system in the original Buckwheat app.
 */
public class Budget {
    
    public enum BudgetStatus {
        ACTIVE("Active"),
        COMPLETED("Completed"),
        EXPIRED("Expired");
        
        private final String displayName;
        
        BudgetStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private Long id;
    private BigDecimal totalAmount;
    private BigDecimal spentAmount;
    private BigDecimal dailyBudget;
    private LocalDate startDate;
    private LocalDate endDate;
    private BudgetStatus status;
    private String description;
    private LocalDate lastDailyBudgetUpdate;
    
    // Constructors
    public Budget() {
        this.spentAmount = BigDecimal.ZERO;
        this.status = BudgetStatus.ACTIVE;
        this.startDate = LocalDate.now();
    }
    
    public Budget(BigDecimal totalAmount, LocalDate startDate, LocalDate endDate) {
        this();
        this.totalAmount = totalAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        calculateInitialDailyBudget();
    }
    
    public Budget(BigDecimal totalAmount, LocalDate startDate, LocalDate endDate, String description) {
        this(totalAmount, startDate, endDate);
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getSpentAmount() {
        return spentAmount;
    }
    
    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }
    
    public BigDecimal getDailyBudget() {
        return dailyBudget;
    }
    
    public void setDailyBudget(BigDecimal dailyBudget) {
        this.dailyBudget = dailyBudget;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public BudgetStatus getStatus() {
        return status;
    }
    
    public void setStatus(BudgetStatus status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getLastDailyBudgetUpdate() {
        return lastDailyBudgetUpdate;
    }
    
    public void setLastDailyBudgetUpdate(LocalDate lastDailyBudgetUpdate) {
        this.lastDailyBudgetUpdate = lastDailyBudgetUpdate;
    }
    
    // Business logic methods
    
    /**
     * Calculate the remaining budget amount
     */
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(spentAmount);
    }
    
    /**
     * Calculate the percentage of budget spent
     */
    public double getSpentPercentage() {
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spentAmount.divide(totalAmount, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100;
    }
    
    /**
     * Calculate remaining days in the budget period
     */
    public long getRemainingDays() {
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) {
            return 0;
        }
        return today.until(endDate).getDays() + 1; // +1 to include today
    }
    
    /**
     * Calculate total days in the budget period
     */
    public long getTotalDays() {
        return startDate.until(endDate).getDays() + 1;
    }
    
    /**
     * Calculate initial daily budget based on total amount and period
     */
    public void calculateInitialDailyBudget() {
        long totalDays = getTotalDays();
        if (totalDays > 0 && totalAmount != null) {
            this.dailyBudget = totalAmount.divide(BigDecimal.valueOf(totalDays), 2, java.math.RoundingMode.HALF_UP);
        }
    }
    
    /**
     * Recalculate daily budget based on remaining amount and days
     */
    public void recalculateDailyBudget() {
        long remainingDays = getRemainingDays();
        if (remainingDays > 0) {
            BigDecimal remainingAmount = getRemainingAmount();
            this.dailyBudget = remainingAmount.divide(BigDecimal.valueOf(remainingDays), 2, java.math.RoundingMode.HALF_UP);
            this.lastDailyBudgetUpdate = LocalDate.now();
        }
    }
    
    /**
     * Check if budget period is active
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return status == BudgetStatus.ACTIVE && 
               (today.isEqual(startDate) || today.isAfter(startDate)) && 
               (today.isEqual(endDate) || today.isBefore(endDate));
    }
    
    /**
     * Check if budget is over the limit
     */
    public boolean isOverBudget() {
        return spentAmount.compareTo(totalAmount) > 0;
    }
    
    /**
     * Add expense to the spent amount
     */
    public void addExpense(BigDecimal amount) {
        this.spentAmount = this.spentAmount.add(amount);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Budget budget = (Budget) obj;
        return Objects.equals(id, budget.id) &&
               Objects.equals(totalAmount, budget.totalAmount) &&
               Objects.equals(startDate, budget.startDate) &&
               Objects.equals(endDate, budget.endDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, totalAmount, startDate, endDate);
    }
    
    @Override
    public String toString() {
        return String.format("Budget{id=%d, total=%s, spent=%s, daily=%s, period=%s to %s, status=%s}",
                id, totalAmount, spentAmount, dailyBudget, startDate, endDate, status);
    }
}
