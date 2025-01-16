package com.tradar.refdata.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.tradar.refdata.model.Symbol;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages reference data for trading symbols including validation rules,
 * tick sizes, and quantity constraints. Loads symbol configurations from
 * external sources and provides validation methods for orders.
 *
 * @author Vrushank Patel
 */
@Slf4j
public class SymbolManager {
    private final Map<String, Symbol> symbols;
    private final ObjectMapper objectMapper;

    public SymbolManager() {
        this.symbols = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
    }

    public void loadSymbols(String configFile) {
        try (InputStream is = getClass().getResourceAsStream(configFile)) {
            CollectionType type = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Symbol.class);
            List<Symbol> symbolList = objectMapper.readValue(is, type);
            
            symbolList.forEach(symbol -> symbols.put(symbol.getSymbol(), symbol));
            log.info("Loaded {} symbols from configuration", symbolList.size());
        } catch (Exception e) {
            log.error("Error loading symbols: {}", e.getMessage(), e);
        }
    }

    public Symbol getSymbol(String symbol) {
        return symbols.get(symbol);
    }

    public List<Symbol> getAllSymbols() {
        return Collections.unmodifiableList(List.copyOf(symbols.values()));
    }

    public boolean isValidSymbol(String symbol) {
        Symbol sym = symbols.get(symbol);
        return sym != null && sym.isActive();
    }

    public boolean isValidPrice(String symbol, java.math.BigDecimal price) {
        Symbol sym = symbols.get(symbol);
        if (sym == null || !sym.isActive()) {
            return false;
        }
        
        // Check if price is multiple of tick size
        return price.remainder(sym.getTickSize())
            .compareTo(java.math.BigDecimal.ZERO) == 0;
    }

    public boolean isValidQuantity(String symbol, java.math.BigDecimal quantity) {
        Symbol sym = symbols.get(symbol);
        if (sym == null || !sym.isActive()) {
            return false;
        }
        
        return quantity.compareTo(sym.getMinQty()) >= 0 &&
               quantity.compareTo(sym.getMaxQty()) <= 0 &&
               quantity.remainder(sym.getLotSize())
                   .compareTo(java.math.BigDecimal.ZERO) == 0;
    }
} 