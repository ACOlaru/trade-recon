package com.traderecon.core;

import java.util.List;

public record MatchResult(MatchStatus matchStatus, String tradeId, Trade tradeA, Trade tradeB,
                          List<String> differences) {

    @Override
    public String toString() {
        return "MatchResult{" +
                "matchStatus=" + matchStatus +
                ", tradeId='" + tradeId + '\'' +
                ", tradeA=" + tradeA +
                ", tradeB=" + tradeB +
                ", differences=" + differences +
                '}';
    }

    @Override
    public MatchStatus matchStatus() {
        return matchStatus;
    }

    @Override
    public String tradeId() {
        return tradeId;
    }

    @Override
    public Trade tradeA() {
        return tradeA;
    }

    @Override
    public Trade tradeB() {
        return tradeB;
    }

    @Override
    public List<String> differences() {
        return differences;
    }
}
