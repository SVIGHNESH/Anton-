package com.moneytracker.util;

import com.moneytracker.database.DatabaseManager;
import com.moneytracker.model.Budget;
import com.moneytracker.service.BudgetService;
import com.moneytracker.service.TransactionService;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDate;

/**
 * Utility class for initializing demo data in the application.
 * This helps new users understand the application features with sample data.
 */
public class DemoDataInitializer {
    
    private final BudgetService budgetService;
    private final TransactionService transactionService;
    private final DatabaseManager databaseManager;
    
    public DemoDataInitializer(BudgetService budgetService, TransactionService transactionService, DatabaseManager databaseManager) {
        this.budgetService = budgetService;
        this.transactionService = transactionService;
        this.databaseManager = databaseManager;
    }
    
    /**
     * Check if the application has any budgets, and if not, create demo data
     */
    public void initializeDemoDataIfNeeded() {
        try {
            // Check if there are any existing budgets
            if (budgetService.getAllBudgets().isEmpty()) {
                createDemoBudgetAndTransactions();
                System.out.println("Demo data initialized successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize demo data: " + e.getMessage());
        }
    }
    
    /**
     * Create a demo budget with sample transactions
     */
    private void createDemoBudgetAndTransactions() throws Exception {
        // Create a demo budget for this month
        LocalDate startDate = LocalDate.now().withDayOfMonth(1); // First day of current month
        LocalDate endDate = startDate.plusMonths(1).minusDays(1); // Last day of current month
        BigDecimal budgetAmount = new BigDecimal("1500.00");
        
        Budget demoBudget = budgetService.createBudget(
            budgetAmount,
            startDate,
            endDate,
            "Demo Budget - Monthly Expenses"
        );
        
        // Add some sample expenses
        addDemoExpenses(demoBudget.getId());
    }
    
    /**
     * Add sample expenses to demonstrate the application features
     */
    private void addDemoExpenses(Long budgetId) throws Exception {
        // Sample expenses with different categories
        Object[][] sampleExpenses = {
            {"Grocery Shopping", new BigDecimal("85.50"), 1L}, // Food & Dining
            {"Gas Station", new BigDecimal("45.00"), 2L}, // Transportation
            {"Coffee Shop", new BigDecimal("12.75"), 1L}, // Food & Dining
            {"Movie Tickets", new BigDecimal("28.00"), 4L}, // Entertainment
            {"Electricity Bill", new BigDecimal("120.00"), 5L}, // Bills & Utilities
            {"Lunch", new BigDecimal("15.25"), 1L}, // Food & Dining
            {"Bus Pass", new BigDecimal("30.00"), 2L}, // Transportation
            {"Book Purchase", new BigDecimal("22.99"), 7L}, // Education
            {"Pharmacy", new BigDecimal("18.50"), 6L}, // Healthcare
            {"Dinner Out", new BigDecimal("65.00"), 1L}, // Food & Dining
        };
        
        for (Object[] expense : sampleExpenses) {
            String description = (String) expense[0];
            BigDecimal amount = (BigDecimal) expense[1];
            Long categoryId = (Long) expense[2];
            
            // Add a small random delay to create different timestamps
            Thread.sleep(100);
            
            transactionService.addExpense(amount, description, categoryId, null, budgetId);
        }
    }
    
    /**
     * Check if demo data exists
     */
    public boolean hasDemoData() {
        try {
            return !budgetService.getAllBudgets().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Clear all demo data (useful for testing)
     */
    public void clearAllData() throws Exception {
        try (PreparedStatement stmt1 = databaseManager.getConnection().prepareStatement("DELETE FROM transactions");
             PreparedStatement stmt2 = databaseManager.getConnection().prepareStatement("DELETE FROM budgets")) {
            
            stmt1.executeUpdate();
            stmt2.executeUpdate();
            
            System.out.println("All data cleared successfully");
        }
    }
}
