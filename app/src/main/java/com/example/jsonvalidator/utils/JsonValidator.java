package com.example.jsonvalidator.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonValidator {

    public static class ValidationResult {
        private final boolean isValid;
        private final int lineNumber;
        private final String errorMessage;

        public ValidationResult(boolean isValid, int lineNumber, String errorMessage) {
            this.isValid = isValid;
            this.lineNumber = lineNumber;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return isValid;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getStatusString() {
            if (isValid) {
                return "OK";
            } else {
                if (lineNumber > 0) {
                    return "ERROR - Line " + lineNumber + " - " + errorMessage;
                } else {
                    return "ERROR - Unknown line - " + errorMessage;
                }
            }
        }
    }

    public static ValidationResult validate(String jsonContent) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            return new ValidationResult(false, -1, "Empty content");
        }

        String trimmed = jsonContent.trim();
        // Root must be Object ({}) or Array ([])
        if (!((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]")))) {
            return new ValidationResult(false, -1, "JSON root must be an Object or Array");
        }

        try {
            JsonElement element = JsonParser.parseString(jsonContent);
            if (element.isJsonObject() || element.isJsonArray()) {
                return new ValidationResult(true, -1, "OK");
            } else {
                return new ValidationResult(false, -1, "JSON root must be an Object or Array");
            }
        } catch (JsonSyntaxException e) {
            String message = e.getMessage();
            int lineNumber = extractLineNumber(message);
            String cleanMessage = cleanErrorMessage(message);
            return new ValidationResult(false, lineNumber, cleanMessage);
        } catch (Exception e) {
            String message = e.getMessage();
            int lineNumber = extractLineNumber(message);
            return new ValidationResult(false, lineNumber, message != null ? message : "Unknown error");
        }
    }

    private static int extractLineNumber(String message) {
        if (message == null) {
            return -1;
        }
        // Gson error format matches "at line X column Y" or "line X"
        Pattern pattern = Pattern.compile("line\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    private static String cleanErrorMessage(String message) {
        if (message == null) {
            return "Malformed JSON";
        }
        // If Gson wraps a MalformedJsonException, extract the end message
        // Example: "com.google.gson.stream.MalformedJsonException: Use JsonReader.setLenient(true)... at line 1 column 5"
        // Let's remove the exception prefix and clean up the tail "at line..."
        String clean = message;
        int lastColon = clean.lastIndexOf("MalformedJsonException:");
        if (lastColon != -1) {
            clean = clean.substring(lastColon + "MalformedJsonException:".length()).trim();
        } else {
            int firstColon = clean.indexOf(":");
            if (firstColon != -1 && firstColon < 100) {
                // Check if it's an exception class name prefix
                String prefix = clean.substring(0, firstColon);
                if (prefix.contains("Exception") || prefix.contains("Error")) {
                    clean = clean.substring(firstColon + 1).trim();
                }
            }
        }

        // Remove the tailing "at line..." part to keep it clean
        int atIndex = clean.lastIndexOf(" at line ");
        if (atIndex != -1) {
            clean = clean.substring(0, atIndex).trim();
        }

        // Clean up common Gson suggestions
        clean = clean.replace("Use JsonReader.setLenient(true) to accept malformed JSON", "Malformed JSON");

        return clean.trim();
    }
}
