package com.tradar.refdata.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class Actor {
    private final String actorId;
    private final String name;
    private final ActorType type;
    private final Set<String> allowedSymbols;
    private final BigDecimal maxOrderValue;
    private final BigDecimal dailyTradingLimit;
    private final boolean active;
} 