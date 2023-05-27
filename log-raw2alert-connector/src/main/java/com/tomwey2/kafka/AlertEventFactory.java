package com.tomwey2.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlertEventFactory {
    private static final Logger logger = LoggerFactory.getLogger(AlertEventFactory.class);
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd HH:mm:ss", Locale.US);
    private static final String timestampRgx = "(?<timestamp>\\w{3}\\s{1,2}\\d{1,2} \\d{2}:\\d{2}:\\d{2})";
    private static final String logDataRgx = ".*(?<type>ALERT|warning:) (?<message>[^$]+)";
    public static final Pattern pattern = Pattern.compile(timestampRgx + logDataRgx);

    public static AlertEvent create(final String logEntry) {
        Matcher matcher = pattern.matcher(logEntry);
        if (!matcher.find()) {
            return null;
        }

        String timestampStr = "2023 " + matcher.group("timestamp");
        if (timestampStr.charAt(9) == ' ') {
            timestampStr = timestampStr.substring(0, 9) + '0' + timestampStr.substring(10);
        }
        LocalDateTime localDateTime = LocalDateTime.parse(timestampStr, formatter);
        return new AlertEvent(localDateTime,
                matcher.group("message"),
                cleanAlertType(matcher.group("type")));

    }

    public static String toJsonString(AlertEvent alertEvent) {
        try {
            return objectMapper.writeValueAsString(alertEvent);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static String cleanAlertType(final String text) {
        return switch (text) {
            case "warning:" -> "WARNING";
            default -> text;
        };
    }
}
