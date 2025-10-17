package com.traderecon.io;

import com.traderecon.core.Trade;
import java.util.List;

public interface TradeLoader {
    List<Trade> loadTrades(String filePath);
}