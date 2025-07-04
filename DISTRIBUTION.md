# Distribution Guide for Anton Money Tracker

## How to Add Packages for Direct Use in GitHub

### 1. Automatic GitHub Releases (Recommended)

The project now includes GitHub Actions that will automatically build and create releases:

#### Setup:
1. Push your code to GitHub
2. Create a tag for release: `git tag v1.0.0 && git push origin v1.0.0`
3. GitHub Actions will automatically:
   - Build the JAR file
   - Run tests
   - Create a GitHub Release
   - Upload the executable JAR

#### Files Created:
- **anton-money-tracker.jar** - Ready-to-run executable JAR (includes all dependencies)

### 2. Manual Release Process

#### Build Locally:
```bash
# Build the executable JAR
mvn clean package shade:shade

# The executable JAR will be created at:
# target/anton-money-tracker.jar
```

#### Use the Release Script:
```bash
# Run the automated release script
./build-release.sh

# This creates:
# - release/anton-money-tracker.jar
# - release/README.txt
# - release/run.bat (Windows)
# - release/run.sh (Linux/Mac)
# - anton-money-tracker-v1.0.0.zip
```

### 3. GitHub Release Steps

1. **Go to your GitHub repository**
2. **Click "Releases" → "Create a new release"**
3. **Tag version:** `v1.0.0`
4. **Release title:** `Anton Money Tracker v1.0.0`
5. **Description:**
   ```markdown
   ## Anton Money Tracker v1.0.0
   
   ### Features
   - ✅ Indian Rupee (₹) currency support
   - ✅ Budget management and expense tracking
   - ✅ Modern JavaFX interface
   - ✅ Local SQLite database
   
   ### Download & Run
   1. Download `anton-money-tracker.jar`
   2. Install Java 17+ from [Adoptium](https://adoptium.net/)
   3. Run: `java -jar anton-money-tracker.jar`
   
   ### System Requirements
   - Java 17 or higher
   - Windows 10+, macOS 10.14+, or Linux
   ```
6. **Upload files:**
   - `anton-money-tracker.jar` (main executable)
   - `anton-money-tracker-v1.0.0.zip` (complete package with scripts)

### 4. Distribution Files

#### Core File:
- **`anton-money-tracker.jar`** - Main executable (self-contained with all dependencies)

#### Optional Helper Files:
- **`README.txt`** - User instructions
- **`run.bat`** - Windows run script
- **`run.sh`** - Linux/Mac run script

### 5. User Instructions to Include

```markdown
## Quick Start for Users

1. **Download:** Get `anton-money-tracker.jar` from GitHub Releases
2. **Install Java:** Download Java 17+ from https://adoptium.net/
3. **Run Application:**
   - **Double-click** the JAR file, or
   - **Command line:** `java -jar anton-money-tracker.jar`

## Troubleshooting

- **Won't start?** Check Java version: `java -version`
- **Need help?** Run from terminal to see error messages
- **JavaFX issues on Linux?** Install: `sudo apt-get install openjfx`
```

### 6. Benefits of This Setup

✅ **Self-contained JAR** - No need to install Maven or build tools
✅ **Cross-platform** - Works on Windows, macOS, and Linux  
✅ **Automatic builds** - GitHub Actions handles building
✅ **Easy updates** - Just tag new versions for automatic releases
✅ **User-friendly** - Simple download and run process
✅ **No database issues** - Each user gets their own local database

### 7. File Sizes

- **anton-money-tracker.jar:** ~35-40MB (includes JavaFX and all dependencies)
- **Database file:** ~50KB (created automatically, grows with data)

### 8. Next Steps

1. **Test the JAR:** `java -jar target/anton-money-tracker.jar`
2. **Commit all files:** Git add, commit, and push
3. **Create first release:** `git tag v1.0.0 && git push origin v1.0.0`
4. **Update README:** Add download links and instructions
5. **Share:** Users can now download and run directly!
