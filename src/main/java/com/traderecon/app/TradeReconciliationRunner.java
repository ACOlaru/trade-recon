package com.traderecon.app;

import com.traderecon.compare.ReconciliationService;
import com.traderecon.config.AppConfig;
import com.traderecon.core.MatchResult;
import com.traderecon.core.Trade;
import com.traderecon.core.TradeCsvLoader;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class TradeReconciliationRunner {
    private static Scanner scanner = new Scanner(System.in);
    private static UserConfig userConfig;

    public static void main(String[] args) {
        runInteractive();
    }

    protected static void runInteractive() {
        UserConfig userConfig = getUserConfig();

        executeReconciliation(userConfig);

        System.out.println("Done");
    }

    private static void executeReconciliation(UserConfig userConfig) {
        AppConfig config = new AppConfig();
        TradeCsvLoader loader = new TradeCsvLoader();

        List<Trade> systemA = loader.loadTrades(userConfig.systemAPath());
        ;
        List<Trade> systemB = loader.loadTrades(userConfig.systemBPath());

        ReconciliationService service = new ReconciliationService(config);

        List<MatchResult> results;
        if (!userConfig.parallel()) {
            results = service.reconcileTrades(systemA, systemB);
        } else {
            results = service.reconcileTradesParallel(systemA, systemB, userConfig.threads());
        }

        if (userConfig.export()) {
            saveResultsToCsv(results, userConfig.outputPath());
            System.out.println("Report saved to " + userConfig.outputPath());
        } else {
            results.forEach(System.out::println);
        }
    }

    private static UserConfig getUserConfig() {
        while (true) {
            printWelcome();

            String systemAPath = askForFile("Enter path for System A file:", "src/main/resources/sample/systemA.csv", false);
            String systemBPath = askForFile("Enter path for System B file:", "src/main/resources/sample/systemB.csv", false);

            boolean parallel = askYesNo("Run in parallel?", false);
            int threads = (parallel ? askForThreads() : 1);
            boolean export = askYesNo("Export results to file?", false);
            String outputPath = (export ? askForFile("Enter output file path:", "results.csv", true) : null);

            printSummary(systemAPath, systemBPath, parallel, threads, export, outputPath);

            boolean confirmed = askYesNo("Continue with reconciliation? (Y to continue, N to restart, Exit to quit)", true);

            if (!confirmed) {
                System.out.println("Restarting the configuration...");
            } else {
               return new UserConfig(systemAPath, systemBPath, parallel, threads, export, outputPath);
            }
        }
    }

    private static void saveResultsToCsv(List<MatchResult> results, String outputPath) {
        Path path = Path.of(outputPath);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("TradeID,Status,SymbolA,ActionA,PriceA,QuantityA,SymbolB,ActionB,PriceB,QuantityB,Differences");
            writer.newLine();

            for (MatchResult match : results) {
                String tradeId = match.tradeId();
                String status = match.matchStatus().name();

                Trade tradeA = match.tradeA();
                Trade tradeB = match.tradeB();

                String symbolA = tradeA != null ? tradeA.symbol() : "";
                String actionA = tradeA != null ? tradeA.action().name() : "";
                String priceA = tradeA != null ? String.valueOf(tradeA.price()) : "";
                String quantityA = tradeA != null ? String.valueOf(tradeA.quantity()) : "";

                String symbolB = tradeB != null ? tradeB.symbol() : "";
                String actionB = tradeB != null ? tradeB.action().name() : "";
                String priceB = tradeB != null ? String.valueOf(tradeB.price()) : "";
                String quantityB = tradeB != null ? String.valueOf(tradeB.quantity()) : "";

                String differences = match.differences() != null ? String.join(";", match.differences()) : "";

                String row = String.join(",", tradeId, status,
                        symbolA, actionA, priceA, quantityA,
                        symbolB, actionB, priceB, quantityB,
                        differences);

                writer.write(row);
                writer.newLine();
            }

            System.out.println("✅ Results successfully saved to: " + path.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
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
        while (true) {
            System.out.println("Please provide number of threads");

            String input = scanner.nextLine().trim().toLowerCase();

            if (input.isEmpty()) {
                return 1;
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please provide a valid number");
            }
        }
    }

    private static boolean askYesNo(String message, boolean defaultValue) {
        while (true) {
            String defaultHint = defaultValue ? " (Y/n/exit): " : " (y/N/exit): ";
            System.out.print(message + defaultHint);

            String input = scanner.nextLine().trim().toLowerCase();

            if (input.isEmpty()) {
                return defaultValue;
            }

            if (input.startsWith("y")) return true;
            if (input.startsWith("n")) return false;
            if (input.startsWith("e")) {
                System.out.println("Exiting....");
                System.exit(0);
            };

            System.out.println("Please answer 'y', 'n' or 'exit'.");
        }
    }

    private static String askForFile(String prompt, String defaultFilename, boolean isOutput) {
        System.out.println(prompt);
        String input = scanner.nextLine().trim();

        String filename = input.isEmpty() ? defaultFilename : input;

        if (!filename.toLowerCase().endsWith(".csv")) {
            System.out.println("⚠️  Only CSV files are supported for now. Please enter a .csv file.");
            return askForFile(prompt, defaultFilename, isOutput);
        }

        Path path;

        if (isOutput) {
            if (Path.of(filename).isAbsolute()) {
                path = Path.of(filename);
            } else {
                path = Path.of("src", "main", "resources", "report", filename);
            }

            try {
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }

                if (!Files.exists(path)) {
                    Files.createFile(path);
                    System.out.println("✅ Created output file at: " + path.toAbsolutePath());
                } else {
                    System.out.println("⚠️  Output file already exists, it will be overwritten.");
                }

                return path.toString();

            } catch (Exception e) {
                System.err.println("Error creating output file: " + e.getMessage());
                System.exit(1);
            }

        } else {
            path = Path.of(filename);
            while (!Files.exists(path)) {
                System.out.print("File not found. Try again or type 'exit' to quit: ");
                input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }

                filename = input;

                if (!filename.toLowerCase().endsWith(".csv")) {
                    System.out.println("⚠️  Only CSV files are supported for now.");
                    continue;
                }

                path = Path.of(filename);
            }

            return path.toString();
        }

        return null;
    }

    private static void printWelcome() {
        System.out.println("Welcome to Trader Reconciliation");
        System.out.println("----------------------");
    }
}

record UserConfig(
        String systemAPath,
        String systemBPath,
        boolean parallel,
        int threads,
        boolean export,
        String outputPath
) {}