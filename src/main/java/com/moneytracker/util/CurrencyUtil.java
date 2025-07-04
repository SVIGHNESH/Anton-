package com.moneytracker.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Utility class for currency formatting in the Indian Rupee (INR) currency
 * Provides consistent currency formatting throughout the application
 */
public class CurrencyUtil {
    
    private static final String CURRENCY_SYMBOL = "₹";
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    
    /**
     * Format a BigDecimal amount as Indian Rupees
     * @param amount the amount to format
     * @return formatted string with rupee symbol (e.g., "₹1,234.56")
     */
    public static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return CURRENCY_SYMBOL + "0.00";
        }
        return CURRENCY_SYMBOL + CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * Format a double amount as Indian Rupees
     * @param amount the amount to format
     * @return formatted string with rupee symbol (e.g., "₹1,234.56")
     */
    public static String formatAmount(double amount) {
        return formatAmount(BigDecimal.valueOf(amount));
    }
    
    /**
     * Format an amount with a prefix (for negative amounts in transactions)
     * @param amount the amount to format
     * @param prefix the prefix to add (e.g., "-")
     * @return formatted string with prefix and rupee symbol (e.g., "-₹1,234.56")
     */
    public static String formatAmountWithPrefix(BigDecimal amount, String prefix) {
        if (amount == null) {
            return prefix + CURRENCY_SYMBOL + "0.00";
        }
        return prefix + CURRENCY_SYMBOL + CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * Get the currency symbol
     * @return the rupee symbol
     */
    public static String getCurrencySymbol() {
        return CURRENCY_SYMBOL;
    }
}
