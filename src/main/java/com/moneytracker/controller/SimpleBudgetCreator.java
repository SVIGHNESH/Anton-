package com.moneytracker.controller;

import com.moneytracker.model.Budget;
import com.moneytracker.service.BudgetService;
import com.moneytracker.util.CurrencyUtil;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Simple controller for budget creation dialog.
 * This is a basic implementation that can be expanded with a proper FXML dialog.
 */
public class SimpleBudgetCreator {
    
    private BudgetService budgetService;
    private Stage dialogStage;
    
    public void setBudgetService(BudgetService budgetService) {
        this.budgetService = budgetService;
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * Show a simple budget creation dialog using built-in JavaFX dialogs
     */
    public static void showBudgetCreationDialog(BudgetService budgetService) {
        // Create input dialogs for budget creation
        TextInputDialog amountDialog = new TextInputDialog("1000.00");
        amountDialog.setTitle("Create New Budget");
        amountDialog.setHeaderText("Budget Setup");
        amountDialog.setContentText("Enter budget amount (₹):");
        
        amountDialog.showAndWait().ifPresent(amountStr -> {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                
                // Get budget description
                TextInputDialog descDialog = new TextInputDialog("Monthly Budget");
                descDialog.setTitle("Budget Description");
                descDialog.setHeaderText("Budget Setup");
                descDialog.setContentText("Enter budget description:");
                
                descDialog.showAndWait().ifPresent(description -> {
                    try {
                        // Create budget for current month (30 days from now)
                        LocalDate startDate = LocalDate.now();
                        LocalDate endDate = startDate.plusDays(30);
                        
                        Budget budget = budgetService.createBudget(amount, startDate, endDate, description);
                        
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("Budget Created");
                        successAlert.setContentText(String.format(
                            "Budget created successfully!\n\nAmount: %s\nPeriod: %s to %s\nDaily Budget: %s",
                            CurrencyUtil.formatAmount(budget.getTotalAmount()),
                            budget.getStartDate(),
                            budget.getEndDate(),
                            CurrencyUtil.formatAmount(budget.getDailyBudget())
                        ));
                        successAlert.showAndWait();
                        
                    } catch (Exception e) {
                        showError("Failed to create budget: " + e.getMessage());
                    }
                });
                
            } catch (NumberFormatException e) {
                showError("Please enter a valid amount (e.g., 1000.00)");
            }
        });
    }
    
    /**
     * Show expense creation dialog
     */
    public static void showExpenseCreationDialog(com.moneytracker.service.TransactionService transactionService, Long budgetId) {
        // Create input dialog for expense amount
        TextInputDialog amountDialog = new TextInputDialog("0.00");
        amountDialog.setTitle("Add Expense");
        amountDialog.setHeaderText("Record Expense");
        amountDialog.setContentText("Enter expense amount (₹):");
        
        amountDialog.showAndWait().ifPresent(amountStr -> {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                
                // Get expense description
                TextInputDialog descDialog = new TextInputDialog("Lunch");
                descDialog.setTitle("Expense Description");
                descDialog.setHeaderText("Record Expense");
                descDialog.setContentText("Enter expense description:");
                
                descDialog.showAndWait().ifPresent(description -> {
                    try {
                        // Add expense (using default category for now)
                        com.moneytracker.model.Transaction transaction = transactionService.addExpense(
                            amount, description, 1L, null, budgetId
                        );
                        
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("Expense Added");
                        successAlert.setContentText(String.format(
                            "Expense recorded successfully!\n\nAmount: %s\nDescription: %s",
                            CurrencyUtil.formatAmount(transaction.getAmount()),
                            transaction.getDescription()
                        ));
                        successAlert.showAndWait();
                        
                    } catch (Exception e) {
                        showError("Failed to add expense: " + e.getMessage());
                    }
                });
                
            } catch (NumberFormatException e) {
                showError("Please enter a valid amount (e.g., 25.99)");
            }
        });
    }
    
    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
