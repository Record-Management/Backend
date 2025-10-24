package com.recordmanagement.habitlog.api.record.dto;

public class SimpleRequest {
    private String message;
    
    public SimpleRequest() {}
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}