package com.hypermall.common.util;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+84|84|0)(3|5|7|8|9)[0-9]{8}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );

    private static final Pattern SLUG_PATTERN = Pattern.compile(
            "^[a-z0-9]+(?:-[a-z0-9]+)*$"
    );

    private ValidationUtil() {
        // Prevent instantiation
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidVietnamesePhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidSlug(String slug) {
        return slug != null && SLUG_PATTERN.matcher(slug).matches();
    }

    public static boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }

    public static boolean isNonNegative(Number number) {
        return number != null && number.doubleValue() >= 0;
    }

    public static boolean isInRange(Number number, Number min, Number max) {
        if (number == null) return false;
        double value = number.doubleValue();
        return value >= min.doubleValue() && value <= max.doubleValue();
    }

    public static boolean isValidRating(Integer rating) {
        return rating != null && rating >= 1 && rating <= 5;
    }

    public static boolean hasMinLength(String str, int minLength) {
        return str != null && str.length() >= minLength;
    }

    public static boolean hasMaxLength(String str, int maxLength) {
        return str == null || str.length() <= maxLength;
    }

    public static boolean isLengthBetween(String str, int min, int max) {
        return str != null && str.length() >= min && str.length() <= max;
    }

    /**
     * Validates Vietnam Tax Code format
     * Format: 10 or 13 digits
     */
    public static boolean isValidTaxCode(String taxCode) {
        if (taxCode == null) return false;
        return taxCode.matches("^\\d{10}$") || taxCode.matches("^\\d{10}-\\d{3}$");
    }

    /**
     * Validates Vietnam Bank Account Number
     * Most banks use 9-16 digits
     */
    public static boolean isValidBankAccount(String accountNumber) {
        if (accountNumber == null) return false;
        return accountNumber.matches("^\\d{9,16}$");
    }
}
