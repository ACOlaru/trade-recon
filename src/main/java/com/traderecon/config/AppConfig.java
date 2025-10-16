package com.traderecon.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class AppConfig {

    private Properties properties;
    private double priceTolerance;
    private double quantityTolerance;
    private boolean caseInsensitiveSymbols;
    private String reportFormat;

    public AppConfig() {
        Properties properties = new Properties();

        init(properties);
    }

    private void init(Properties properties) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                setDefaults();
                System.out.println("config.properties not found, using defaults");
            } else {
                properties.load(input);
                this.properties = properties;
                parsePriceTolerance();
                parseQuantityTolerance();
                parseCaseInsensitiveSymbols();
                parseReportFormat();
            }
        } catch (IOException e) {
            setDefaults();
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    private void setDefaults() {
        this.priceTolerance = 0.0001;
        this.quantityTolerance = 0;
        this.caseInsensitiveSymbols = true;
        this.reportFormat = "CSV";
    }

    private void parsePriceTolerance() {
        String priceString = properties.getProperty("priceTolerance");

        if (priceString == null) {
            this.priceTolerance = 0.0001;
        } else {
            try {
                this.priceTolerance = Double.parseDouble(priceString);
            } catch (NumberFormatException e) {
                this.priceTolerance = 0.0001;
            }
        }
    }

    private void parseQuantityTolerance() {
        String quantityTolerance = properties.getProperty("quantityTolerance");

        if (quantityTolerance == null) {
            this.quantityTolerance = 0;
        } else {
            try {
                this.quantityTolerance = Double.parseDouble(quantityTolerance);
            } catch (NumberFormatException e) {
                this.quantityTolerance = 0;
            }
        }
    }

    private void parseCaseInsensitiveSymbols() {
        String caseInsensitiveSymbols = properties.getProperty("compareSymbolsCaseInsensitive");

        if (caseInsensitiveSymbols == null) {
            this.caseInsensitiveSymbols = true;
        } else {
            try {
                this.caseInsensitiveSymbols = Boolean.parseBoolean(caseInsensitiveSymbols);
            } catch (NumberFormatException e) {
                this.caseInsensitiveSymbols = true;
            }
        }
    }

    private void parseReportFormat() {
        String reportFormat = properties.getProperty("reportFormat");
        this.reportFormat = Objects.requireNonNullElse(reportFormat, "CSV");
    }

    public Properties getProperties() {
        return properties;
    }

    public double getPriceTolerance() {
        return priceTolerance;
    }

    public double getQuantityTolerance() {
        return quantityTolerance;
    }

    public boolean isCaseInsensitiveSymbols() {
        return caseInsensitiveSymbols;
    }

    public String getReportFormat() {
        return reportFormat;
    }

    //TODO
    public void reload() {
        this.properties = new Properties();
        init(properties);
    }

}
