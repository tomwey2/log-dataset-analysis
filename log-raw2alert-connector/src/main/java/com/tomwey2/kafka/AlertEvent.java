package com.tomwey2.kafka;

import java.io.Serializable;

public class AlertEvent implements Serializable {
    private String alertType;
    private String message;
    private String timestamp;
    
    public AlertEvent() {
    }

    public AlertEvent(String alertType, String message, String timestamp) {
        this.alertType = alertType;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
