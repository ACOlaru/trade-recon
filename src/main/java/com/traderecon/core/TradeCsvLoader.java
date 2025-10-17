package com.traderecon.core;

import com.traderecon.io.TradeLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TradeCsvLoader implements TradeLoader {

    @Override
    public List<Trade> loadTrades(String filePath) {
        List<Trade> trades = new ArrayList<>();

        try (Stream<String> lines = Files.lines(Path.of(filePath))) {
            trades = lines
                    .skip(1)
                    .map(this::parseTrade)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            System.err.println("❌ Error reading file: " + filePath + " - " + e.getMessage());
        }

        return trades;
    }

    private Trade parseTrade(String line) {
        try {
            String[] parts = line.split(",");

            if (parts.length < 5) {
                System.err.println("Skipping malformed line: " + line);
                return null;
            }

            String tradeId = parts[0].trim();
            String symbol = parts[1].trim();
            ActionEnum action = ActionEnum.valueOf(parts[2].trim().toUpperCase());
            double price = Double.parseDouble(parts[3].trim());
            double quantity = Double.parseDouble(parts[4].trim());
//            LocalDateTime timestamp = LocalDateTime.parse(parts[5].trim());
            LocalDateTime timestamp = LocalDateTime.now();

            return new Trade(tradeId, symbol, price, quantity, action, timestamp);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to parse line: " + line + " (" + e.getMessage() + ")");
            return null;
        }
    }
}
