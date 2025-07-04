package com.moneytracker.controller;

import com.moneytracker.model.Budget;
import com.moneytracker.model.Category;
import com.moneytracker.model.Transaction;
import com.moneytracker.service.BudgetService;
import com.moneytracker.service.CategoryService;
import com.moneytracker.service.TransactionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main controller for the Money Tracker application.
 * Handles the primary UI interactions and orchestrates between services.
 */
public class MainController {
    
    // Services
    private BudgetService budgetService;
    private TransactionService transactionService;
    private CategoryService categoryService;
    
    // FXML Components - Dashboard Tab
    @FXML private TabPane mainTabPane;
    @FXML private Label currentBudgetLabel;
    @FXML private Label spentAmountLabel;
    @FXML private Label remainingAmountLabel;
    @FXML private Label dailyBudgetLabel;
    @FXML private ProgressBar budgetProgressBar;
    @FXML private Label progressPercentageLabel;
    @FXML private Button newBudgetButton;
    @FXML private Button addExpenseButton;
    @FXML private VBox dashboardContent;
    
    // FXML Components - Transactions Tab
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, String> amountColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private Button editTransactionButton;
    @FXML private Button deleteTransactionButton;
    
    // FXML Components - Analytics Tab
    @FXML private PieChart categorySpendingChart;
    @FXML private Label totalExpensesLabel;
    @FXML private Label averageDailySpendingLabel;
    @FXML private Label biggestExpenseLabel;
    
    // Data
    private ObservableList<Transaction> transactionData = FXCollections.observableArrayList();
    private Budget currentBudget;
    
    /**
     * Initialize the controller with services
     */
    public void initialize(BudgetService budgetService, TransactionService transactionService) {
        this.budgetService = budgetService;
        this.transactionService = transactionService;
        // We'll initialize categoryService later when we need it
        
        setupUI();
        loadCurrentBudget();
        refreshData();
    }
    
    /**
     * Setup UI components and event handlers
     */
    private void setupUI() {
        // Setup transactions table
        setupTransactionsTable();
        
        // Setup button event handlers
        newBudgetButton.setOnAction(e -> showNewBudgetDialog());
        addExpenseButton.setOnAction(e -> showAddExpenseDialog());
        editTransactionButton.setOnAction(e -> editSelectedTransaction());
        deleteTransactionButton.setOnAction(e -> deleteSelectedTransaction());
        
        // Setup table selection listener
        transactionsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                editTransactionButton.setDisable(!hasSelection);
                deleteTransactionButton.setDisable(!hasSelection);
            }
        );
        
        // Initially disable edit/delete buttons
        editTransactionButton.setDisable(true);
        deleteTransactionButton.setDisable(true);
    }
    
    /**
     * Setup the transactions table columns
     */
    private void setupTransactionsTable() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTimestamp().format(dateFormatter)));
        
        descriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        
        categoryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory() != null ? 
                cellData.getValue().getCategory() : "Uncategorized"));
        
        amountColumn.setCellValueFactory(cellData -> {
            BigDecimal amount = cellData.getValue().getAmount();
            String formattedAmount = String.format("$%.2f", amount);
            if (cellData.getValue().getType() == Transaction.TransactionType.EXPENSE) {
                formattedAmount = "-" + formattedAmount;
            }
            return new SimpleStringProperty(formattedAmount);
        });
        
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType().getDisplayName()));
        
        transactionsTable.setItems(transactionData);
    }
    
    /**
     * Load the current active budget
     */
    private void loadCurrentBudget() {
        try {
            Optional<Budget> budgetOpt = budgetService.getCurrentBudget();
            if (budgetOpt.isPresent()) {
                currentBudget = budgetOpt.get();
                updateBudgetDisplay();
                addExpenseButton.setDisable(false);
            } else {
                // No active budget - show create budget prompt
                currentBudget = null;
                showNoBudgetState();
                addExpenseButton.setDisable(true);
            }
        } catch (Exception e) {
            showErrorAlert("Error Loading Budget", "Failed to load current budget: " + e.getMessage());
        }
    }
    
    /**
     * Update the budget display in the dashboard
     */
    private void updateBudgetDisplay() {
        if (currentBudget == null) {
            showNoBudgetState();
            return;
        }
        
        // Update labels
        currentBudgetLabel.setText(String.format("$%.2f", currentBudget.getTotalAmount()));
        spentAmountLabel.setText(String.format("$%.2f", currentBudget.getSpentAmount()));
        remainingAmountLabel.setText(String.format("$%.2f", currentBudget.getRemainingAmount()));
        dailyBudgetLabel.setText(String.format("$%.2f", currentBudget.getDailyBudget()));
        
        // Update progress bar
        double spentPercentage = currentBudget.getSpentPercentage() / 100.0;
        budgetProgressBar.setProgress(spentPercentage);
        progressPercentageLabel.setText(String.format("%.1f%%", currentBudget.getSpentPercentage()));
        
        // Set progress bar color based on spending
        if (spentPercentage > 0.9) {
            budgetProgressBar.setStyle("-fx-accent: #ff4757;"); // Red for high spending
        } else if (spentPercentage > 0.7) {
            budgetProgressBar.setStyle("-fx-accent: #ffa726;"); // Orange for moderate spending
        } else {
            budgetProgressBar.setStyle("-fx-accent: #2ed573;"); // Green for low spending
        }
    }
    
    /**
     * Show state when no budget is active
     */
    private void showNoBudgetState() {
        currentBudgetLabel.setText("No Active Budget");
        spentAmountLabel.setText("$0.00");
        remainingAmountLabel.setText("$0.00");
        dailyBudgetLabel.setText("$0.00");
        budgetProgressBar.setProgress(0);
        progressPercentageLabel.setText("0%");
    }
    
    /**
     * Refresh all data displays
     */
    private void refreshData() {
        loadTransactions();
        updateAnalytics();
    }
    
    /**
     * Load transactions for the current budget
     */
    private void loadTransactions() {
        try {
            transactionData.clear();
            if (currentBudget != null) {
                List<Transaction> transactions = transactionService.getTransactionsByBudget(currentBudget.getId());
                transactionData.addAll(transactions);
            }
        } catch (Exception e) {
            showErrorAlert("Error Loading Transactions", "Failed to load transactions: " + e.getMessage());
        }
    }
    
    /**
     * Update analytics display
     */
    private void updateAnalytics() {
        if (currentBudget == null) {
            categorySpendingChart.getData().clear();
            totalExpensesLabel.setText("$0.00");
            averageDailySpendingLabel.setText("$0.00");
            biggestExpenseLabel.setText("$0.00");
            return;
        }
        
        try {
            // Update category spending chart
            Map<String, BigDecimal> spendingByCategory = transactionService.getSpendingByCategory(currentBudget.getId());
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            for (Map.Entry<String, BigDecimal> entry : spendingByCategory.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
            }
            
            categorySpendingChart.setData(pieChartData);
            
            // Update summary statistics
            BudgetService.BudgetSummary summary = budgetService.calculateBudgetSummary(currentBudget.getId());
            if (summary != null) {
                totalExpensesLabel.setText(String.format("$%.2f", summary.getTotalExpenses()));
                
                // Calculate average daily spending
                long daysElapsed = Math.max(1, currentBudget.getTotalDays() - currentBudget.getRemainingDays());
                BigDecimal averageDaily = summary.getTotalExpenses().divide(BigDecimal.valueOf(daysElapsed), 2, java.math.RoundingMode.HALF_UP);
                averageDailySpendingLabel.setText(String.format("$%.2f", averageDaily));
                
                // Find biggest expense
                BigDecimal biggestExpense = findBiggestExpense();
                biggestExpenseLabel.setText(String.format("$%.2f", biggestExpense));
            }
            
        } catch (Exception e) {
            showErrorAlert("Error Updating Analytics", "Failed to update analytics: " + e.getMessage());
        }
    }
    
    /**
     * Find the biggest single expense
     */
    private BigDecimal findBiggestExpense() {
        return transactionData.stream()
            .filter(Transaction::isExpense)
            .map(Transaction::getAmount)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }
    
    /**
     * Show new budget creation dialog
     */
    private void showNewBudgetDialog() {
        // For now, show a simple input dialog
        showInfoAlert("New Budget", "Budget creation dialog will be implemented in the next update.");
        // TODO: Implement proper budget dialog
    }
    
    /**
     * Show add expense dialog
     */
    private void showAddExpenseDialog() {
        if (currentBudget == null) {
            showInfoAlert("No Active Budget", "Please create a budget first before adding expenses.");
            return;
        }
        
        // For now, show a simple input dialog
        showInfoAlert("Add Expense", "Expense dialog will be implemented in the next update.");
        // TODO: Implement proper expense dialog
    }
    
    /**
     * Edit selected transaction
     */
    private void editSelectedTransaction() {
        Transaction selectedTransaction = transactionsTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) return;
        
        // TODO: Implement edit transaction dialog
        showInfoAlert("Edit Transaction", "Edit transaction functionality will be implemented in a future update.");
    }
    
    /**
     * Delete selected transaction
     */
    private void deleteSelectedTransaction() {
        Transaction selectedTransaction = transactionsTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) return;
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Transaction");
        confirmDialog.setHeaderText("Are you sure you want to delete this transaction?");
        confirmDialog.setContentText(selectedTransaction.getDescription() + " - $" + selectedTransaction.getAmount());
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                transactionService.deleteTransaction(selectedTransaction.getId());
                loadCurrentBudget(); // Reload budget to get updated spent amount
                refreshData();
                showInfoAlert("Success", "Transaction deleted successfully.");
            } catch (Exception e) {
                showErrorAlert("Error", "Failed to delete transaction: " + e.getMessage());
            }
        }
    }
    
    /**
     * Utility methods for showing alerts
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
