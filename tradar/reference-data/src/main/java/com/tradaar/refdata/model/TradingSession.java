package com.tradar.refdata.model;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
public class TradingSession {
    private final String sessionId;
    private final String description;
    private final Set<DayOfWeek> tradingDays;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final LocalTime preOpen;
    private final LocalTime preClose;
    private final boolean active;
} 