package com.traderecon.compare;

import com.traderecon.config.AppConfig;
import com.traderecon.core.MatchResult;
import com.traderecon.core.MatchStatus;
import com.traderecon.core.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class ReconciliationTask implements Callable<List<MatchResult>> {
    protected Set<String> partition;
    protected Map<String, Trade> mapA;
    protected Map<String, Trade> mapB;
    protected AppConfig appConfig;
    protected ReconciliationService reconciliationService;

    public ReconciliationTask(Set<String> partition, Map<String, Trade> mapA, Map<String, Trade> mapB, AppConfig appConfig, ReconciliationService reconciliationService) {
        this.partition = partition;
        this.mapA = mapA;
        this.mapB = mapB;
        this.appConfig = appConfig;
        this.reconciliationService = reconciliationService;
    }

    @Override
    public List<MatchResult> call() throws Exception {
        List<MatchResult> matchResults = new ArrayList<MatchResult>();

        for (String tradeId : partition) {
            Trade tradeA = mapA.get(tradeId);
            Trade tradeB = mapB.get(tradeId);

            if (tradeA != null && tradeB != null) {
                matchResults.add(reconciliationService.compareTrades(tradeA, tradeB));
            } else if (tradeA == null) {
                matchResults.add(new MatchResult(MatchStatus.MISSING_IN_A, tradeId, null, tradeB, reconciliationService.getMissingTradeIdDifferencesAsList()));
            } else {
                matchResults.add(new MatchResult(MatchStatus.MISSING_IN_B, tradeId, tradeA, null, reconciliationService.getMissingTradeIdDifferencesAsList()));
            }
        }

        return matchResults;
    }
}
