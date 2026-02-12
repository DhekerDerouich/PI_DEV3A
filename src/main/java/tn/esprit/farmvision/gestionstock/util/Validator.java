package tn.esprit.farmvision.gestionstock.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class Validator {

    public static boolean isValidString(String value, int minLength, int maxLength) {
        return value != null &&
                value.trim().length() >= minLength &&
                value.trim().length() <= maxLength;
    }

    public static boolean isValidDouble(String value, double min, double max) {
        try {
            double number = Double.parseDouble(value);
            return number >= min && number <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidInteger(String value, int min, int max) {
        try {
            int number = Integer.parseInt(value);
            return number >= min && number <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{8,15}");
    }

    public static boolean isValidDate(LocalDate date) {
        return date != null && !date.isAfter(LocalDate.now());
    }

    public static boolean isValidPrice(String price) {
        return isValidDouble(price, 0.01, 1000000);
    }

    public static boolean isValidQuantity(String quantity) {
        return isValidDouble(quantity, 0.01, 1000000);
    }

    public static String getStringError(String fieldName, int min, int max) {
        return fieldName + " doit contenir entre " + min + " et " + max + " caractères.";
    }

    public static String getNumberError(String fieldName, double min, double max) {
        return fieldName + " doit être entre " + min + " et " + max + ".";
    }

    public static String getRequiredError(String fieldName) {
        return fieldName + " est obligatoire.";
    }
}