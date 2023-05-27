package com.tomwey2.kafka;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

class AlertEventFactoryTest {
    private final Map<String, String[]> testLogs = Map.of(
            "Jun 29 04:03:12 combo logrotate: ALERT exited abnormally with [1]",
            new String[]{"Jun 29 04:03:12", "exited abnormally with [1]", "ALERT"},
            "Jul 25 23:23:13 combo xinetd[26482]: warning: can't get client address: Connection reset by peer",
            new String[]{"Jul 25 23:23:13", "can't get client address: Connection reset by peer", "warning:"},
            "Jun 25 09:20:24 combo ftpd[31463]: connection from 210.118.170.95 () at Sat Jun 25 09:20:24 2005",
            new String[]{"", "", ""}
    );

    @Test
    public void transformToStringTest() {
        for (Map.Entry<String, String[]> entry : testLogs.entrySet()) {
            AlertEvent alertEvent = AlertEventFactory.create(entry.getKey());
            if (entry.getKey().contains("ALERT") || entry.getKey().contains("warning")) {
                assertEquals(entry.getValue()[1], alertEvent.message());
                assertEquals(AlertEventFactory.cleanAlertType(entry.getValue()[2]), alertEvent.alertType());
                String json = AlertEventFactory.toJsonString(alertEvent);
                System.out.println(json);
            } else {
                assertNull(alertEvent);
            }
        }
    }

    @Test
    public void regexTest() {
        for (Map.Entry<String, String[]> entry : testLogs.entrySet()) {
            Matcher matcher = AlertEventFactory.pattern.matcher(entry.getKey());
            if (entry.getKey().contains("ALERT") || entry.getKey().contains("warning")) {
                assertTrue(matcher.find());
                assertEquals(entry.getValue()[0], matcher.group("timestamp"));
                assertEquals(entry.getValue()[1], matcher.group("message"));
                assertEquals(entry.getValue()[2], matcher.group("type"));
            }
        }
    }
}