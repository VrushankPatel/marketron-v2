package com.tradar.marketdata.fix;

import com.tradar.marketdata.cache.MarketDataCache;
import com.tradar.marketdata.model.MarketDataEntry;
import lombok.extern.slf4j.Slf4j;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.MarketDataSnapshotFullRefresh;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class MarketDataClient extends MessageCracker implements Application {
    private final MarketDataCache cache;
    private Session session;
    private final List<String> subscribedSymbols;

    public MarketDataClient(MarketDataCache cache) {
        this.cache = cache;
        this.subscribedSymbols = new ArrayList<>();
    }

    public void subscribeMarketData(String symbol) {
        if (session != null && session.isLoggedOn()) {
            MarketDataRequest request = new MarketDataRequest();
            request.set(new MDReqID(UUID.randomUUID().toString()));
            request.set(new SubscriptionRequestType('1')); // Snapshot + Updates
            request.set(new MarketDepth(1));
            request.set(new MDUpdateType(0)); // Full refresh

            MarketDataRequest.NoMDEntryTypes types = new MarketDataRequest.NoMDEntryTypes();
            types.set(new MDEntryType(MDEntryType.BID));
            request.addGroup(types);
            types.set(new MDEntryType(MDEntryType.OFFER));
            request.addGroup(types);
            types.set(new MDEntryType(MDEntryType.TRADE));
            request.addGroup(types);

            MarketDataRequest.NoRelatedSym symbols = new MarketDataRequest.NoRelatedSym();
            symbols.set(new Symbol(symbol));
            request.addGroup(symbols);

            boolean sent = session.send(request);
            if (!sent) {
                log.error("Failed to send market data request for symbol: {}", symbol);
                return;
            }
            subscribedSymbols.add(symbol);
            log.info("Subscribed to market data for symbol: {}", symbol);
        }
    }

    public void onMessage(MarketDataSnapshotFullRefresh message, SessionID sessionID) throws FieldNotFound {
        String symbol = message.get(new Symbol()).getValue();
        MarketDataEntry.MarketDataEntryBuilder builder = MarketDataEntry.builder()
                .symbol(symbol)
                .timestamp(LocalDateTime.now());

        int entries = message.getGroupCount(NoMDEntries.FIELD);
        for (int i = 1; i <= entries; i++) {
            MarketDataSnapshotFullRefresh.NoMDEntries group = new MarketDataSnapshotFullRefresh.NoMDEntries();
            message.getGroup(i, group);

            char type = group.get(new MDEntryType()).getValue();
            BigDecimal price = new BigDecimal(group.get(new MDEntryPx()).getValue());
            BigDecimal size = new BigDecimal(group.get(new MDEntrySize()).getValue());

            switch (type) {
                case MDEntryType.BID:
                    builder.bidPrice(price).bidSize(size);
                    break;
                case MDEntryType.OFFER:
                    builder.askPrice(price).askSize(size);
                    break;
                case MDEntryType.TRADE:
                    builder.lastPrice(price).lastSize(size);
                    break;
            }
        }

        cache.updateMarketData(builder.build());
    }

    // QuickFIX Application interface methods
    @Override
    public void onCreate(SessionID sessionID) {
        log.info("Session created: {}", sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {
        log.info("Logged on: {}", sessionID);
        this.session = Session.lookupSession(sessionID);
        // Resubscribe to symbols after reconnect
        for (String symbol : subscribedSymbols) {
            subscribeMarketData(symbol);
        }
    }

    @Override
    public void onLogout(SessionID sessionID) {
        log.info("Logged out: {}", sessionID);
        this.session = null;
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {}

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {}

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {}

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionID);
    }
} 