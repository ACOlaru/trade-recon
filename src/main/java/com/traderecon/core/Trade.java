package com.traderecon.core;

import java.time.LocalDateTime;
import java.util.Objects;

public record Trade(String tradeId, String symbol, double price, double quantity, ActionEnum action,
                    LocalDateTime timestamp) {

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId='" + tradeId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", action=" + action +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return Objects.equals(tradeId, trade.tradeId);
    }

}
