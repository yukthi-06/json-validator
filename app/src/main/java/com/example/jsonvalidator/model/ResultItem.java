package com.example.jsonvalidator.model;

public class ResultItem {
    private final String filePath;
    private final String status;
    private final boolean isValid;
    private final int lineNumber;
    private final String errorMessage;

    public ResultItem(String filePath, String status, boolean isValid, int lineNumber, String errorMessage) {
        this.filePath = filePath;
        this.status = status;
        this.isValid = isValid;
        this.lineNumber = lineNumber;
        this.errorMessage = errorMessage;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getStatus() {
        return status;
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
}
