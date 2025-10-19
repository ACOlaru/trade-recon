package com.traderecon.app;

import com.traderecon.compare.ReconciliationService;
import com.traderecon.config.AppConfig;
import com.traderecon.core.MatchResult;
import com.traderecon.core.Trade;
import com.traderecon.core.TradeCsvLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TradeReconciliationRunner {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        runInteractive();
    }

    protected static void runInteractive() {
        printWelcome();
        String systemAPath = askForFile("Please provide file name for System A","src/main/resources/sample/systemA.csv");
        String systemBPath = askForFile("Please provide file name for System B","src/main/resources/sample/systemB.csv");

        boolean parallel = askYesNo("Run in parallel?", false);
        int threads = (parallel ? askForThreads() : 1);
        boolean export = askYesNo("Export results to file?", false);
        String outputPath = (export ? askForFile("Please provide file name for output file", "src/main/resources/output/output.csv") : null);

        printSummary(systemAPath, systemBPath, parallel, threads, export, outputPath);
        boolean confirmed = askYesNo("Continue with reconciliation?", true);

        if (!confirmed) {
            System.out.println("Exiting...");
            System.exit(0);
        }


        AppConfig config = new AppConfig();
        TradeCsvLoader loader = new TradeCsvLoader();

        List<Trade> systemA = loader.loadTrades(systemAPath);;
        List<Trade> systemB = loader.loadTrades(systemBPath);

        ReconciliationService service = new ReconciliationService(config);

        List<MatchResult> results = new ArrayList<>();
        if (!parallel) {
            results = service.reconcileTrades(systemA, systemB);
        } else {
            results = service.reconcileTradesParallel(systemA, systemB, threads);
        }

        if (export) {
            saveResultsToCsv(results, outputPath);
            System.out.println("Report saved to " + outputPath);
        } else {
            results.forEach(System.out::println);
        }

        System.out.println("Done");
    }

    private static void saveResultsToCsv(List<MatchResult> results, String outputPath) {
        return;
    }

    private static void printSummary(
            String systemAPath,
            String systemBPath,
            boolean parallel,
            int threads,
            boolean export,
            String outputPath) {

        System.out.println("\nSummary of inputs:");
        System.out.println("-------------------");

        System.out.println("System A file: " + systemAPath);
        System.out.println("System B file: " + systemBPath);

        if (parallel) {
            System.out.println("Parallel mode: yes (threads: " + threads + ")");
        } else {
            System.out.println("Parallel mode: no");
        }

        if (export) {
            System.out.println("Export results: yes (output: " + outputPath + ")");
        } else {
            System.out.println("Export results: no");
        }

        System.out.println("-------------------\n");
    }

    private static int askForThreads() {
        return 0;
    }

    private static boolean askYesNo(String message, boolean defaultValue) {
        while (true) {
            String defaultHint = defaultValue ? " (Y/n): " : " (y/N): ";
            System.out.print(message + defaultHint);

            String input = scanner.nextLine().trim().toLowerCase();

            if (input.isEmpty()) {
                return defaultValue;
            }

            if (input.startsWith("y")) return true;
            if (input.startsWith("n")) return false;

            System.out.println("Please answer 'y' or 'n'.");
        }
    }


    private static String askForFile(String prompt, String defaultPath) {
        System.out.println(prompt);
        String input = scanner.nextLine().trim();

        String filename = input.isEmpty() ? defaultPath : input;

        while (!Files.exists(Path.of(filename))) {
            System.out.print("File not found. Try again or type 'exit' to quit: ");
            input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting...");
                System.exit(0);
            }

            filename = input;
        }

        return filename;
    }

    private static void printWelcome() {
        System.out.println("Welcome to Trader Reconciliation");
        System.out.println("----------------------");
    }

}
