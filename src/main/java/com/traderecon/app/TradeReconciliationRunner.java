package com.traderecon.app;

import com.traderecon.compare.ReconciliationService;
import com.traderecon.config.AppConfig;
import com.traderecon.core.MatchResult;
import com.traderecon.core.Trade;
import com.traderecon.core.TradeCsvLoader;

import java.util.List;

public class TradeReconciliationRunner {
    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        TradeCsvLoader loader = new TradeCsvLoader();


        List<Trade> systemA = loader.loadTrades("src/main/resources/sample/systemA.csv");;
        List<Trade> systemB = loader.loadTrades("src/main/resources/sample/systemB.csv");

        ReconciliationService service = new ReconciliationService(config);
        List<MatchResult> results = service.reconcileTrades(systemA, systemB);

        results.forEach(System.out::println);
    }

}
