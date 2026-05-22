package com.example.travelease.util;

import java.util.regex.Pattern;

public class ValidationHelper {

    // Mobile validation: exactly 10 digits, only 0-9 allowed
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    // Email validation: standard structure ending with .com
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[c][o][m]$", Pattern.CASE_INSENSITIVE);

    // Password validation: at least one uppercase, one lowercase, one numeric digit
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_NUMERIC = Pattern.compile("[0-9]");

    public static boolean isValidMobileNumber(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return PASSWORD_UPPERCASE.matcher(password).find() &&
               PASSWORD_LOWERCASE.matcher(password).find() &&
               PASSWORD_NUMERIC.matcher(password).find();
    }
}
