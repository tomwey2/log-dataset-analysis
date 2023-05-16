package com.tomwey2.kafka;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertEventFactoryTest {

    @Test
    public void testAlertEventFactoryCreatesAlert() {
        String log = "Jul 27 04:16:09 combo logrotate: ALERT exited abnormally with [1]";
        AlertEvent alertEvent = AlertEventFactory.create(log);
        assertEquals("ALERT", alertEvent.getAlertType());
        assertEquals("Jul 27 04:16:09", alertEvent.getTimestamp());
        assertEquals("ALERT exited abnormally with [1]", alertEvent.getMessage());
    }

    @Test
    public void testAlertEventFactoryCreatesWarning() {
        String log = "Jul 25 23:23:13 combo xinetd[26482]: warning: can't get client address: Connection reset by peer";
        AlertEvent alertEvent = AlertEventFactory.create(log);
        assertEquals("WARNING", alertEvent.getAlertType());
        assertEquals("Jul 25 23:23:13", alertEvent.getTimestamp());
        assertEquals("warning: can't get client address: Connection reset by peer", alertEvent.getMessage());
    }

    @Test
    public void testAlertEventFactoryCreatesUnknown() {
        String log = "Jun 25 09:20:24 combo ftpd[31463]: connection from 210.118.170.95 () at Sat Jun 25 09:20:24 2005";
        AlertEvent alertEvent = AlertEventFactory.create(log);
        assertEquals("UNKNOWN", alertEvent.getAlertType());
        assertEquals("Jun 25 09:20:24", alertEvent.getTimestamp());
        assertEquals("", alertEvent.getMessage());
    }
}