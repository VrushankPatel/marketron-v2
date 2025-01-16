package com.tradar.refdata.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradar.refdata.model.Actor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ActorManager {
    private final Map<String, Actor> actors;
    private final Map<String, BigDecimal> dailyTrading;
    private final ObjectMapper objectMapper;

    public ActorManager() {
        this.actors = new ConcurrentHashMap<>();
        this.dailyTrading = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
    }

    public void loadActors(String configFile) {
        try (InputStream is = getClass().getResourceAsStream(configFile)) {
            List<Actor> actorList = objectMapper.readValue(is,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Actor.class));
            
            actorList.forEach(actor -> actors.put(actor.getActorId(), actor));
            log.info("Loaded {} actors from configuration", actorList.size());
        } catch (Exception e) {
            log.error("Error loading actors: {}", e.getMessage(), e);
        }
    }

    public boolean canTradeSymbol(String actorId, String symbol) {
        Actor actor = actors.get(actorId);
        return actor != null && actor.isActive() && 
               actor.getAllowedSymbols().contains(symbol);
    }

    public boolean checkOrderValue(String actorId, BigDecimal orderValue) {
        Actor actor = actors.get(actorId);
        return actor != null && actor.isActive() && 
               orderValue.compareTo(actor.getMaxOrderValue()) <= 0;
    }

    public boolean checkDailyLimit(String actorId, BigDecimal tradeValue) {
        Actor actor = actors.get(actorId);
        if (actor == null || !actor.isActive()) {
            return false;
        }

        BigDecimal currentTotal = dailyTrading.getOrDefault(actorId, BigDecimal.ZERO);
        return currentTotal.add(tradeValue).compareTo(actor.getDailyTradingLimit()) <= 0;
    }

    public void recordTrade(String actorId, BigDecimal tradeValue) {
        dailyTrading.compute(actorId, (id, current) -> 
            current == null ? tradeValue : current.add(tradeValue));
    }

    public void resetDailyLimits() {
        dailyTrading.clear();
    }

    public List<Actor> getAllActors() {
        return List.copyOf(actors.values());
    }
} 