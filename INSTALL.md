# Anton Money Tracker - Installation Guide

## Quick Download & Run

### For End Users (Recommended)

1. **Download the latest release:**
   - Go to [Releases](https://github.com/SVIGHNESH/Anton/releases)
   - Download `anton-money-tracker.jar`

2. **Install Java (if not already installed):**
   - Download Java 17+ from [Adoptium](https://adoptium.net/)
   - Install following the installer instructions

3. **Run the application:**
   - **Windows:** Double-click `anton-money-tracker.jar` or run `java -jar anton-money-tracker.jar`
   - **macOS:** Double-click `anton-money-tracker.jar` or run `java -jar anton-money-tracker.jar`
   - **Linux:** Run `java -jar anton-money-tracker.jar` in terminal

## For Developers

### Building from Source

```bash
# Clone the repository
git clone https://github.com/SVIGHNESH/Anton.git
cd Anton

# Build the project
mvn clean package

# Run the application
mvn javafx:run

# Or run the generated JAR
java -jar target/anton-money-tracker.jar
```

### Creating a Release Package

```bash
# Use the provided build script
./build-release.sh

# Or manually
mvn clean package shade:shade
```

## System Requirements

- **Java:** Version 17 or higher
- **RAM:** Minimum 256MB, Recommended 512MB
- **Storage:** 100MB free space
- **OS:** Windows 10+, macOS 10.14+, Linux (any modern distribution)

## Troubleshooting

### Application Won't Start

1. **Check Java version:**
   ```bash
   java -version
   ```
   Should show version 17 or higher.

2. **Run from command line to see errors:**
   ```bash
   java -jar anton-money-tracker.jar
   ```

3. **JavaFX issues on Linux:**
   ```bash
   # Install JavaFX if needed
   sudo apt-get install openjfx
   ```

### Database Issues

- The application creates `anton_money_tracker.db` in the same directory
- If corrupted, delete the file and restart (demo data will be recreated)
- For backup, copy the `.db` file

### Performance Issues

- Increase Java heap size:
  ```bash
  java -Xmx1G -jar anton-money-tracker.jar
  ```

## Features

- ✅ Budget management with Indian Rupee (₹) support
- ✅ Expense tracking and categorization  
- ✅ Real-time budget calculations
- ✅ Visual analytics with charts
- ✅ Local SQLite database
- ✅ Demo data for new users
- ✅ Modern JavaFX interface

## Data Location

- **Database:** `anton_money_tracker.db` (same folder as JAR)
- **No external dependencies** - everything is self-contained

## Version History

### v1.0.0
- Initial release
- Indian Rupee currency support
- Basic budget and expense management
- JavaFX-based modern UI
