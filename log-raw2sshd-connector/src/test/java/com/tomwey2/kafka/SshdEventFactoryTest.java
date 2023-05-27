package com.tomwey2.kafka;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SshdEventFactoryTest {
    private final Map<String, String[]> testLogs = Map.of(
            "Jun 21 08:56:36 combo sshd(pam_unix)[14278]: authentication failure; logname= uid=0 euid=0 tty=NODEVssh ruser= rhost=217.60.212.66  user=guest",
            new String[]{"Jun 21 08:56:36", "authentication failure", "217.60.212.66", "guest"},
            "Jun 14 15:16:02 combo sshd(pam_unix)[19937]: authentication failure; logname= uid=0 euid=0 tty=NODEVssh ruser= rhost=218.188.2.4",
            new String[]{"Jun 14 15:16:02", "authentication failure", "218.188.2.4", null},
            "Jun 30 22:16:32 combo sshd(pam_unix)[19432]: session opened for user test by (uid=509)",
            new String[]{"Jun 30 22:16:32", "session opened for user test by (uid=509)", null, null},
            "Jul  1 00:21:28 combo sshd(pam_unix)[19630]: authentication failure; logname= uid=0 euid=0 tty=NODEVssh ruser= rhost=60.30.224.116  user=root",
            new String[]{"Jul  1 00:21:28", "authentication failure", "60.30.224.116", "root"}
    );

    @Test
    public void transformToStringTest() {
        for (Map.Entry<String, String[]> entry : testLogs.entrySet()) {
            SshdEvent sshdEvent = SshdEventFactory.create(entry.getKey());
            assertEquals(entry.getValue()[1], sshdEvent.message());
            assertEquals(entry.getValue()[2], sshdEvent.host());
            assertEquals(entry.getValue()[3], sshdEvent.user());
            String json = SshdEventFactory.toJsonString(sshdEvent);
            //System.out.println(json);
        }
    }

    @Test
    public void regexTest() {
        for (Map.Entry<String, String[]> entry : testLogs.entrySet()) {
            Matcher matcher = SshdEventFactory.pattern.matcher(entry.getKey());
            assertTrue(matcher.find());
            assertEquals(entry.getValue()[0], matcher.group("timestamp"));
            assertEquals(entry.getValue()[1], matcher.group("message"));
            assertEquals(entry.getValue()[2], matcher.group("host"));
            assertEquals(entry.getValue()[3], matcher.group("user"));
        }
    }

}