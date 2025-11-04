package pe.edu.upeu.sysventas.utils;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

import java.util.regex.Pattern;

/**
 * Validador en tiempo real que proporciona feedback inmediato
 * Mejora la experiencia de usuario mostrando errores mientras escribe
 */
public class RealtimeFieldValidator {

    private static final String ERROR_STYLE = "-fx-border-color: #dc3545; -fx-border-width: 2px; -fx-background-color: #ffeeee;";
    private static final String SUCCESS_STYLE = "-fx-border-color: #28a745; -fx-border-width: 2px;";
    private static final String NORMAL_STYLE = "";

    /**
     * Configura validación en tiempo real para un campo de texto
     */
    public static void setupRealtimeValidation(TextField field, Label feedbackLabel, ValidationType type) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            ValidationResult result = validate(newValue, type);
            updateFieldStyle(field, result);
            updateFeedbackLabel(feedbackLabel, result);
        });
    }

    /**
     * Valida un valor según el tipo especificado
     */
    private static ValidationResult validate(String value, ValidationType type) {
        if (value == null || value.trim().isEmpty()) {
            return new ValidationResult(false, "Este campo es requerido", ValidationLevel.ERROR);
        }

        switch (type) {
            case EMAIL:
                return validateEmail(value);
            case PHONE:
                return validatePhone(value);
            case DNI:
                return validateDNI(value);
            case RUC:
                return validateRUC(value);
            case NUMERIC:
                return validateNumeric(value);
            case POSITIVE_NUMBER:
                return validatePositiveNumber(value);
            case TEXT_ONLY:
                return validateTextOnly(value);
            case ALPHANUMERIC:
                return validateAlphanumeric(value);
            case PASSWORD:
                return validatePassword(value);
            default:
                return new ValidationResult(true, "", ValidationLevel.SUCCESS);
        }
    }

    private static ValidationResult validateEmail(String email) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        if (pattern.matcher(email).matches()) {
            return new ValidationResult(true, "✓ Email válido", ValidationLevel.SUCCESS);
        }
        return new ValidationResult(false, "✗ Email inválido", ValidationLevel.ERROR);
    }

    private static ValidationResult validatePhone(String phone) {
        Pattern pattern = Pattern.compile("^[0-9]{9,10}$");
        if (pattern.matcher(phone).matches()) {
            return new ValidationResult(true, "✓ Teléfono válido", ValidationLevel.SUCCESS);
        }
        return new ValidationResult(false, "✗ Debe tener 9-10 dígitos", ValidationLevel.ERROR);
    }

    private static ValidationResult validateDNI(String dni) {
        if (dni.length() == 8 && dni.matches("[0-9]{8}")) {
            return new ValidationResult(true, "✓ DNI válido", ValidationLevel.SUCCESS);
        }
        return new ValidationResult(false, "✗ DNI debe tener 8 dígitos", ValidationLevel.ERROR);
    }

    private static ValidationResult validateRUC(String ruc) {
        if (ruc.length() == 11 && ruc.matches("[0-9]{11}")) {
            return new ValidationResult(true, "✓ RUC válido", ValidationLevel.SUCCESS);
        }
        return new ValidationResult(false, "✗ RUC debe tener 11 dígitos", ValidationLevel.ERROR);
    }

    private static ValidationResult validateNumeric(String value) {
        try {
            Double.parseDouble(value);
            return new ValidationResult(true, "✓ Número válido", ValidationLevel.SUCCESS);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "✗ Debe ser un número", ValidationLevel.ERROR);
        }
    }

    private static ValidationResult validatePositiveNumber(String value) {
        try {
            double num = Double.parseDouble(value);
            if (num > 0) {
                return new ValidationResult(true, "✓ Número válido", ValidationLevel.SUCCESS);
            }
            return new ValidationResult(false, "✗ Debe ser mayor a 0", ValidationLevel.ERROR);
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "✗ Debe ser un número", ValidationLevel.ERROR);
        }
    }

    private static ValidationResult validateTextOnly(String value) {
        if (value.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            return new ValidationResult(true, "✓ Texto válido", ValidationLevel.SUCCESS);
        }
        return new ValidationResult(false, "✗ Solo se permiten letras", ValidationLevel.ERROR);
    }

    private static ValidationResult validateAlphanumeric(String value) {
        if (value.matches("^[a-zA-Z0-9\\s]+$")) {
            return new ValidationResult(true, "✓ Texto válido", ValidationLevel.SUCCESS);
        }
        return new ValidationResult(false, "✗ Solo letras y números", ValidationLevel.ERROR);
    }

    private static ValidationResult validatePassword(String password) {
        if (password.length() < 6) {
            return new ValidationResult(false, "✗ Mínimo 6 caracteres", ValidationLevel.ERROR);
        }
        if (password.length() < 8) {
            return new ValidationResult(true, "⚠ Contraseña débil", ValidationLevel.WARNING);
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        int strength = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);

        if (strength >= 3) {
            return new ValidationResult(true, "✓ Contraseña fuerte", ValidationLevel.SUCCESS);
        } else if (strength >= 2) {
            return new ValidationResult(true, "⚠ Contraseña media", ValidationLevel.WARNING);
        }
        return new ValidationResult(false, "✗ Contraseña débil", ValidationLevel.ERROR);
    }

    private static void updateFieldStyle(TextField field, ValidationResult result) {
        switch (result.level) {
            case SUCCESS:
                field.setStyle(SUCCESS_STYLE);
                break;
            case WARNING:
                field.setStyle("-fx-border-color: #ffc107; -fx-border-width: 2px;");
                break;
            case ERROR:
                field.setStyle(ERROR_STYLE);
                break;
        }

        Tooltip tooltip = new Tooltip(result.message);
        field.setTooltip(tooltip);
    }

    private static void updateFeedbackLabel(Label label, ValidationResult result) {
        if (label == null) return;

        label.setText(result.message);
        switch (result.level) {
            case SUCCESS:
                label.setTextFill(Color.GREEN);
                break;
            case WARNING:
                label.setTextFill(Color.ORANGE);
                break;
            case ERROR:
                label.setTextFill(Color.RED);
                break;
        }
    }

    // Enums y clases auxiliares
    public enum ValidationType {
        EMAIL, PHONE, DNI, RUC, NUMERIC, POSITIVE_NUMBER,
        TEXT_ONLY, ALPHANUMERIC, PASSWORD
    }

    enum ValidationLevel {
        SUCCESS, WARNING, ERROR
    }

    static class ValidationResult {
        boolean isValid;
        String message;
        ValidationLevel level;

        ValidationResult(boolean isValid, String message, ValidationLevel level) {
            this.isValid = isValid;
            this.message = message;
            this.level = level;
        }
    }
}