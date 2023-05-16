package com.tomwey2.kafka;

public class AlertEventFactory {
    public static AlertEvent create(final String message) {
        AlertEvent alertEvent;
        String timestr = message.substring(0, 15);
        String datastr = message.substring(message.indexOf(":", 15) + 2);
        if (datastr.startsWith("ALERT")) {
            alertEvent = new AlertEvent("ALERT", datastr, timestr);
        } else if (datastr.startsWith("warning")) {
            alertEvent = new AlertEvent("WARNING", datastr, timestr);
        } else {
            alertEvent = new AlertEvent("UNKNOWN", "", timestr);
        }
        return alertEvent;
    }
}
