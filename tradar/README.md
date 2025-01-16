# Tradaar Trading Platform

A high-performance, modular trading platform built in Java that implements a complete trading system with order entry, matching engine, market data distribution, and trade reporting capabilities.

## System Architecture

The platform consists of the following core modules:

- **Core**: Common models and utilities used across all modules
- **Order Entry**: GUI application for submitting trading orders
- **Matching Engine**: Implements price-time priority matching algorithm
- **Market Data**: Real-time market data distribution and display
- **Reference Data**: Symbol and instrument management
- **Reporting**: Trade and execution reporting interface

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Bash shell (for running scripts)
- Network that supports multicast communication


## Getting Started

### Initial Setup

1. Clone the repository:

```bash
git clone https://github.com/yourusername/tradaar.git
cd tradaar
```

2. Run the setup script to create necessary directories:

```bash
./scripts/setup.sh
```

3. Check your development environment:

```bash
./scripts/dev.sh check
```


This will verify:
- Java 17+ installation
- Maven installation
- Required system configurations

### Development Environment Setup

1. Initialize the development environment:

```bash
./scripts/dev.sh setup
```


This will:
- Create necessary directories (logs, data)
- Resolve Maven dependencies
- Set up development configurations

2. Build the project:

```bash
./scripts/build.sh
```


The build script:
- Builds the parent project
- Builds core module first
- Builds other modules in parallel
- Copies dependencies to appropriate locations

### Running the System

#### Development Mode

1. Start all components in development mode:

```bash
./scripts/run.sh start-dev all

# Or start individual components:

./scripts/run.sh start-dev market-data
./scripts/run.sh start-dev order-entry
./scripts/run.sh start-dev reporting
```

### Production Mode

1. Start all components in production mode:

```bash
./scripts/run.sh start all
```

2. Start specific components:

```bash
./scripts/run.sh start market-data
./scripts/run.sh start matching-engine
```

3. Check component status:

```bash
./scripts/run.sh status
```

4. Stop components:

```bash
./scripts/run.sh stop all
or
./scripts/run.sh stop order-entry
```


### Component Startup Order

For proper system operation, start components in this order:

1. Reference Data
2. Market Data
3. Matching Engine
4. Reporting
5. Order Entry

### Monitoring and Logs

All logs are stored in `scripts/logs/` directory:

```bash
tail -f scripts/logs/market-data.log # Market data logs
tail -f scripts/logs/order-entry.log # Order entry logs
tail -f scripts/logs/reporting.log # Reporting logs
```

Process IDs are stored in `scripts/data/`:

```bash
cat scripts/data/order-entry.pid # Order Entry process ID
```


## Component Details

### Order Entry (OrderEntryForm)
- GUI interface for order submission
- Supports market and limit orders
- Real-time order status updates
- Validates orders before submission
- Uses multicast UDP for order transmission
- Configurable order parameters:
  - Symbol
  - Side (Buy/Sell)
  - Order Type (Market/Limit)
  - Quantity
  - Price
  - Time in Force

### Market Data (MarketDataViewer)
- Real-time market data display
- Symbol subscription management
- Price level aggregation
- Displays:
  - Bid/Ask prices and sizes
  - Last trade price and size
  - Volume
  - High/Low prices
  - Timestamp
- Auto-refresh functionality
- Multicast data reception

### Reporting (ReportViewer)
- Tabbed interface for different report types
- Real-time execution reports
- Trade capture report viewing
- Filtering capabilities:
  - By Order ID
  - By Symbol
  - By Time Range
- Auto-refresh every 5 seconds
- Historical data access

### Reference Data (SymbolManager)
- Manages trading symbols
- Validates:
  - Price tick sizes
  - Lot sizes
  - Min/Max quantities
  - Symbol status
- JSON-based configuration
- Thread-safe implementation

## Network Configuration

The system uses multicast communication:

```java
public class NetworkConfig {
    public static final String MULTICAST_GROUP = "239.0.0.1";
    public static final int ORDER_ENTRY_PORT = 5001;
    public static final int MARKET_DATA_PORT = 5002;
    public static final int EXECUTION_REPORT_PORT = 5003;
    public static final int TRADE_REPORT_PORT = 5004;
}
```

### Development Workflow

1. Make code changes
2. Clean the project if needed:

```bash
./scripts/clean.sh
```
3. Run tests:

```bash
Run all tests
./scripts/dev.sh test
```

```bash
Run specific module tests
./scripts/dev.sh test -m order-entry
```
4. Rebuild and restart:
```bash
./scripts/build.sh
./scripts/run.sh restart all
```

### Troubleshooting

1. If components fail to start:
   - Check logs in `scripts/logs/`
   - Verify all PIDs in `scripts/data/`
   - Ensure no port conflicts
   - Verify multicast network support

2. If builds fail:
   - Run `./scripts/clean.sh`
   - Check Maven settings
   - Verify Java version
   - Check disk space

3. Common Issues:
   - Port already in use: Check running processes
   - Connection refused: Verify network settings
   - ClassNotFoundException: Rebuild the project

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Author

Vrushank Patel

## License

This project is licensed under the MIT License - see the LICENSE file for details

