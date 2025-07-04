package com.moneytracker.controller;

import com.moneytracker.controller.SimpleBudgetCreator;
import com.moneytracker.model.Budget;
import com.moneytracker.model.Transaction;
import com.moneytracker.service.BudgetService;
import com.moneytracker.service.TransactionService;
import com.moneytracker.util.CurrencyUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
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
     * JavaFX initialize method called automatically after FXML loading
     */
    @FXML
    private void initialize() {
        // Setup UI components only - services will be injected later
        setupUI();
    }
    
    /**
     * Initialize the controller with services (called manually from App)
     */
    public void initializeServices(BudgetService budgetService, TransactionService transactionService) {
        this.budgetService = budgetService;
        this.transactionService = transactionService;
        
        loadCurrentBudget();
        refreshData();
    }
    
    /**
     * Setup UI components and event handlers
     */
    private void setupUI() {
        // Setup transactions table only if it exists
        if (transactionsTable != null) {
            setupTransactionsTable();
        }
        
        // Setup button event handlers only if buttons exist
        if (newBudgetButton != null) {
            newBudgetButton.setOnAction(e -> showNewBudgetDialog());
        }
        if (addExpenseButton != null) {
            addExpenseButton.setOnAction(e -> showAddExpenseDialog());
        }
        if (editTransactionButton != null) {
            editTransactionButton.setOnAction(e -> editSelectedTransaction());
        }
        if (deleteTransactionButton != null) {
            deleteTransactionButton.setOnAction(e -> deleteSelectedTransaction());
        }
        
        // Setup table selection listener only if all components exist
        if (transactionsTable != null && editTransactionButton != null && deleteTransactionButton != null) {
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
    }
    
    /**
     * Setup the transactions table columns
     */
    private void setupTransactionsTable() {
        if (dateColumn == null || descriptionColumn == null || categoryColumn == null || 
            amountColumn == null || typeColumn == null || transactionsTable == null) {
            return; // Skip setup if any components are missing
        }
        
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
            String formattedAmount = CurrencyUtil.formatAmount(amount);
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
        currentBudgetLabel.setText(CurrencyUtil.formatAmount(currentBudget.getTotalAmount()));
        spentAmountLabel.setText(CurrencyUtil.formatAmount(currentBudget.getSpentAmount()));
        remainingAmountLabel.setText(CurrencyUtil.formatAmount(currentBudget.getRemainingAmount()));
        dailyBudgetLabel.setText(CurrencyUtil.formatAmount(currentBudget.getDailyBudget()));
        
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
        spentAmountLabel.setText(CurrencyUtil.formatAmount(BigDecimal.ZERO));
        remainingAmountLabel.setText(CurrencyUtil.formatAmount(BigDecimal.ZERO));
        dailyBudgetLabel.setText(CurrencyUtil.formatAmount(BigDecimal.ZERO));
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
            totalExpensesLabel.setText(CurrencyUtil.formatAmount(BigDecimal.ZERO));
            averageDailySpendingLabel.setText(CurrencyUtil.formatAmount(BigDecimal.ZERO));
            biggestExpenseLabel.setText(CurrencyUtil.formatAmount(BigDecimal.ZERO));
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
                totalExpensesLabel.setText(CurrencyUtil.formatAmount(summary.getTotalExpenses()));
                
                // Calculate average daily spending
                long daysElapsed = Math.max(1, currentBudget.getTotalDays() - currentBudget.getRemainingDays());
                BigDecimal averageDaily = summary.getTotalExpenses().divide(BigDecimal.valueOf(daysElapsed), 2, java.math.RoundingMode.HALF_UP);
                averageDailySpendingLabel.setText(CurrencyUtil.formatAmount(averageDaily));
                
                // Find biggest expense
                BigDecimal biggestExpense = findBiggestExpense();
                biggestExpenseLabel.setText(CurrencyUtil.formatAmount(biggestExpense));
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
        SimpleBudgetCreator.showBudgetCreationDialog(budgetService);
        // Refresh data after dialog closes
        loadCurrentBudget();
        refreshData();
    }
    
    /**
     * Show add expense dialog
     */
    private void showAddExpenseDialog() {
        if (currentBudget == null) {
            showInfoAlert("No Active Budget", "Please create a budget first before adding expenses.");
            return;
        }
        
        SimpleBudgetCreator.showExpenseCreationDialog(transactionService, currentBudget.getId());
        // Refresh data after dialog closes
        loadCurrentBudget(); // Reload budget to get updated spent amount
        refreshData();
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
        confirmDialog.setContentText(selectedTransaction.getDescription() + " - " + CurrencyUtil.formatAmount(selectedTransaction.getAmount()));
        
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
