package com.traderecon.compare;

import com.traderecon.config.AppConfig;
import com.traderecon.core.MatchResult;
import com.traderecon.core.MatchStatus;
import com.traderecon.core.Trade;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReconciliationService {

    private final AppConfig appConfig;

    public ReconciliationService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public List<MatchResult> reconcileTrades(List<Trade> systemA, List<Trade> systemB) {
        System.out.println("ReconciliationService reconcileTrades");
        Map<String, Trade> mapA = mapTrades(systemA);
        Map<String, Trade> mapB = mapTrades(systemB);

        Set<String> allTradeIds = new HashSet<>();
        allTradeIds.addAll(mapA.keySet());
        allTradeIds.addAll(mapB.keySet());

        return buildReconciliationResults(allTradeIds, mapA, mapB);
    }

    private List<MatchResult> buildReconciliationResults(Set<String> allTradeIds, Map<String, Trade> mapA, Map<String, Trade> mapB) {
        List<MatchResult> matchResults = new ArrayList<>();
        for (String tradeId : allTradeIds) {
            Trade tradeA = mapA.get(tradeId);
            Trade tradeB = mapB.get(tradeId);

            if (tradeA != null && tradeB != null) {
                matchResults.add(compareTrades(tradeA, tradeB));
            } else if (tradeA == null) {
                matchResults.add(new MatchResult(MatchStatus.MISSING_IN_A, tradeId, null, tradeB, getMissingTradeIdDifferencesAsList()));
            } else {
                matchResults.add(new MatchResult(MatchStatus.MISSING_IN_B, tradeId, tradeA, null, getMissingTradeIdDifferencesAsList()));
            }
        }

        return matchResults;
    }

    public List<String> getMissingTradeIdDifferencesAsList() {
        return Arrays.asList(
                "Action missing",
                "Symbol missing",
                "Price missing",
                "Quantity missing"
        );
    }

    protected Map<String, Trade> mapTrades(List<Trade> system) {
        Map<String, Trade> result = new HashMap<>();

        for (Trade trade : system) {
            String tradeId = trade.tradeId();
            if (result.containsKey(tradeId)) {
                System.err.println("Duplicate trade id: " + tradeId);
            } else {
                result.put(tradeId, trade);
            }
        }

        return result;
    }


    protected MatchResult compareTrades(Trade tradeA, Trade tradeB) {
       List<String> differences = getDifferences(tradeA, tradeB);

        MatchStatus status = differences.isEmpty() ? MatchStatus.MATCHED : MatchStatus.MISMATCHED;

        return new MatchResult(status, tradeA.tradeId(), tradeA, tradeB, differences);
    }


    protected List<String> getDifferences(Trade tradeA, Trade tradeB) {
        List<String> differences = new ArrayList<>();

        if (!tradeA.action().equals(tradeB.action())) {
            differences.add("Action " + tradeA.action() + " != Action " + tradeB.action());
        }

        if (appConfig.isCaseInsensitiveSymbols()) {
            if (!tradeA.symbol().equalsIgnoreCase(tradeB.symbol())) {
                differences.add("Symbol " + tradeA.symbol() + " != Symbol " + tradeB.symbol());
            }
        } else {
            if (!tradeA.symbol().equals(tradeB.symbol())) {
                differences.add("Symbol " + tradeA.symbol() + " != Symbol " + tradeB.symbol());
            }
        }

        if (Math.abs(tradeA.price() - tradeB.price()) > appConfig.getPriceTolerance()) {
            differences.add("Price " + tradeA.price() + " != " + tradeB.price());
        }

        if (Math.abs(tradeA.quantity() - tradeB.quantity()) > appConfig.getQuantityTolerance()) {
            differences.add("Quantity " + tradeA.quantity() + " != Quantity " + tradeB.quantity());
        }

        return differences;
    }

    public List<MatchResult> reconcileTradesParallel(List<Trade> systemA, List<Trade> systemB, int numberOfThreads) {
        Map<String, Trade> mapA = mapTrades(systemA);
        Map<String, Trade> mapB = mapTrades(systemB);

        Set<String> allTradeIds = new HashSet<>();
        allTradeIds.addAll(mapA.keySet());
        allTradeIds.addAll(mapB.keySet());

        List<Set<String>> partitions = partitionTradeIds(allTradeIds, numberOfThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<List<MatchResult>>> futures = new ArrayList<>();

        for (Set<String> partition : partitions) {
            System.out.println("Submitting a new task");
            ReconciliationTask task = new ReconciliationTask(partition, mapA, mapB, appConfig, this);
            futures.add(executor.submit(task));

        }

        List<MatchResult> allResults = new ArrayList<>();
        for (Future<List<MatchResult>> future : futures) {
            try {
                allResults.addAll(future.get());
            } catch (ExecutionException | InterruptedException e) {
                System.err.println("Error in thread: " + e.getMessage());
            }
        }

        executor.shutdown();

        return allResults;
    }

    private List<Set<String>> partitionTradeIds(Set<String> allTradeIds, int numberOfThreads) {
        List<Set<String>> partitions = new ArrayList<>();
        int chunkSize = Math.max(1, (int) Math.ceil((double) allTradeIds.size() / numberOfThreads));
        Set<String> currentChunk = new HashSet<>();

        for (String tradeId : allTradeIds) {
            currentChunk.add(tradeId);
            if (currentChunk.size() == chunkSize) {
                partitions.add(currentChunk);
                currentChunk = new HashSet<>();
            }
        }

        if (!currentChunk.isEmpty()) {
            partitions.add(currentChunk);
        }


        return partitions;
    }
}
