package com.moneytracker;

import com.moneytracker.controller.MainController;
import com.moneytracker.database.DatabaseManager;
import com.moneytracker.service.BudgetService;
import com.moneytracker.service.TransactionService;
import com.moneytracker.util.DemoDataInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Anton Java - Money Tracker Application
 * A comprehensive budgeting and expense tracking application.
 * 
 * Features:
 * - Budget management with period-based tracking
 * - Daily budget calculation and redistribution
 * - Expense tracking with categories
 * - Analytics and spending visualization
 * - Modern JavaFX interface
 */
public class AntonApp extends Application {
    
    private static final String APP_TITLE = "Anton Java - Money Tracker";
    private static final String MAIN_FXML = "/fxml/main_basic.fxml";
    private static final String APP_ICON = "/images/app-icon.png";
    
    private DatabaseManager databaseManager;
    private BudgetService budgetService;
    private TransactionService transactionService;
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Initialize database and services
        initializeServices();
        
        // Load FXML and create scene
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_FXML));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        
        // Get controller and inject services
        MainController controller = fxmlLoader.getController();
        controller.initializeServices(budgetService, transactionService);
        
        // Configure stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Set application icon
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
        } catch (Exception e) {
            System.out.println("Could not load application icon: " + e.getMessage());
        }
        
        // Show the stage
        primaryStage.show();
        
        // Handle application close
        primaryStage.setOnCloseRequest(event -> {
            shutdown();
        });
    }
    
    private void initializeServices() {
        try {
            // Initialize database manager
            databaseManager = new DatabaseManager();
            databaseManager.initializeDatabase();
                 // Initialize services
        budgetService = new BudgetService(databaseManager);
        transactionService = new TransactionService(databaseManager);
        
        // Initialize demo data if needed
        DemoDataInitializer demoInitializer = new DemoDataInitializer(budgetService, transactionService, databaseManager);
        demoInitializer.initializeDemoDataIfNeeded();
        
        System.out.println("Services initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize services: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void shutdown() {
        try {
            if (databaseManager != null) {
                databaseManager.closeConnection();
            }
            System.out.println("Application shutdown complete");
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
