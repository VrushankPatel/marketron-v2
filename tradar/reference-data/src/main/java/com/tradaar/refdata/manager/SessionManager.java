package com.tradar.refdata.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tradar.refdata.model.TradingSession;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SessionManager {
    private final Map<String, TradingSession> sessions;
    private final ObjectMapper objectMapper;

    public SessionManager() {
        this.sessions = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    }

    public void loadSessions(String configFile) {
        try (InputStream is = getClass().getResourceAsStream(configFile)) {
            List<TradingSession> sessionList = objectMapper.readValue(is,
                objectMapper.getTypeFactory().constructCollectionType(List.class, TradingSession.class));
            
            sessionList.forEach(session -> sessions.put(session.getSessionId(), session));
            log.info("Loaded {} trading sessions from configuration", sessionList.size());
        } catch (Exception e) {
            log.error("Error loading trading sessions: {}", e.getMessage(), e);
        }
    }

    public boolean isWithinTradingHours(LocalDateTime timestamp) {
        return sessions.values().stream()
            .filter(TradingSession::isActive)
            .anyMatch(session -> {
                if (!session.getTradingDays().contains(timestamp.getDayOfWeek())) {
                    return false;
                }
                
                LocalDateTime sessionStart = timestamp.toLocalDate()
                    .atTime(session.getStartTime());
                LocalDateTime sessionEnd = timestamp.toLocalDate()
                    .atTime(session.getEndTime());
                
                return !timestamp.isBefore(sessionStart) && !timestamp.isAfter(sessionEnd);
            });
    }

    public boolean isPreOpen(LocalDateTime timestamp) {
        return sessions.values().stream()
            .filter(TradingSession::isActive)
            .anyMatch(session -> {
                if (!session.getTradingDays().contains(timestamp.getDayOfWeek())) {
                    return false;
                }
                
                LocalDateTime preOpen = timestamp.toLocalDate()
                    .atTime(session.getPreOpen());
                LocalDateTime start = timestamp.toLocalDate()
                    .atTime(session.getStartTime());
                
                return !timestamp.isBefore(preOpen) && !timestamp.isAfter(start);
            });
    }

    public List<TradingSession> getAllSessions() {
        return List.copyOf(sessions.values());
    }
} 