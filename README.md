# Trade Reconciliation Tool

A Java application to **reconcile trades** between two systems, detect mismatches, and export results. Designed for traders and engineers to quickly identify discrepancies in trade data.

---

## Table of Contents

1. [Overview](#overview)
2. [Key Highlights](#key-highlights)  
3. [Features](#features)  
4. [Requirements](#requirements)  
5. [Usage](#usage)  
6. [Configuration](#configuration)
7. [Future Improvements](#future-improvements)
8. [License](#license)  

---

## Overview

This application reads trade data from two CSV files (System A and System B), compares the trades by **trade ID, symbol, price, quantity, and action**, and outputs a reconciliation report highlighting:

- Matched trades  
- Mismatched trades  
- Missing trades in either system  

It supports **interactive CLI prompts** and **multi-threaded reconciliation** for large datasets.

---

## Key Highlights

- **Core Skills Demonstrated:** Java 17+, object-oriented design, records, streams, and exception handling  
- **Multithreading:** Parallel reconciliation using `ExecutorService` for faster processing on large datasets  
- **Clean Architecture:** Separation of concerns: `core` (models), `compare` (services), `config`, `io`, and `app`  
- **Configurable:** Supports case-insensitive symbols, price/quantity tolerance via `config.properties`  
- **CLI Interface:** Interactive command-line prompts with restart, confirmation, and export options  

---

## Features

- Compare trades across two systems  
- Configurable **case-insensitive symbols**  
- Configurable **tolerance for price and quantity differences**  
- Export results to CSV (`resources/report/`)  
- Optional multi-threaded processing  
- Interactive CLI with restart option  

---

## Requirements

- Java 17+  
- Maven 3.8+  
- Works on Windows, Mac, and Linux  

---

## Usage

The application is **CLI-based**:

1. You will be prompted for the **System A and System B file paths** (CSV files).  
2. Choose whether to **run reconciliation in parallel**.  
3. Choose whether to **export results to a CSV file**.  
4. Confirm your inputs or restart if needed.  
5. The application will display results on the console or save them to the specified output CSV.

**Default sample files (for quick testing):**

- src/main/resources/sample/systemA.csv
- src/main/resources/sample/systemB.csv

**Example CSV format:**

csv
tradeId,symbol,side,quantity,price
T001,AAPL,BUY,100,180.5
T002,GOOG,SELL,50,2700.1
T003,MSFT,BUY,30,300.0

## Configuration
Optional settings can be adjusted in config.properties:

- caseInsensitiveSymbols=true
- priceTolerance=0.01
- quantityTolerance=0.01


This allows traders to tune the reconciliation logic without changing code.

## Future Improvements

- Support additional file formats (Excel, JSON)
- GUI or web interface for easier use
- Real-time trade streaming and reconciliation
- Enhanced logging and analytics for reports
- Configurable output folder and filename patterns

## License

MIT License
