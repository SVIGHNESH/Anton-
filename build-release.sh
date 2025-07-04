#!/bin/bash

# Anton Money Tracker - Build and Release Script

echo "🚀 Building Anton Money Tracker for Release..."

# Clean previous builds
echo "🧹 Cleaning previous builds..."
mvn clean

# Run tests
echo "🧪 Running tests..."
mvn test

if [ $? -ne 0 ]; then
    echo "❌ Tests failed! Please fix the issues before creating a release."
    exit 1
fi

# Build the application
echo "🔨 Building application..."
mvn package shade:shade

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

# Create release directory
echo "📦 Preparing release package..."
RELEASE_DIR="release"
rm -rf $RELEASE_DIR
mkdir -p $RELEASE_DIR

# Copy the executable JAR
cp target/anton-money-tracker.jar $RELEASE_DIR/

# Create README for release
cat > $RELEASE_DIR/README.txt << 'EOF'
# Anton Money Tracker

## Quick Start

1. Make sure you have Java 17 or higher installed
   - Download from: https://adoptium.net/

2. Run the application:
   - Double-click on anton-money-tracker.jar, or
   - Run from command line: java -jar anton-money-tracker.jar

## Features

- Budget management with Indian Rupee (₹) support
- Expense tracking and categorization
- Analytics and spending insights
- Local SQLite database storage

## System Requirements

- Java 17 or higher
- Windows, macOS, or Linux
- At least 100MB of free disk space

## Support

For issues and questions, please visit:
https://github.com/[your-username]/Anton

## Version

Version: 1.0.0
Build Date: $(date)
EOF

# Create run scripts for different platforms
cat > $RELEASE_DIR/run.bat << 'EOF'
@echo off
echo Starting Anton Money Tracker...
java -jar anton-money-tracker.jar
pause
EOF

cat > $RELEASE_DIR/run.sh << 'EOF'
#!/bin/bash
echo "Starting Anton Money Tracker..."
java -jar anton-money-tracker.jar
EOF

chmod +x $RELEASE_DIR/run.sh

# Create ZIP archive
echo "📦 Creating release archive..."
cd $RELEASE_DIR
zip -r "../anton-money-tracker-v1.0.0.zip" .
cd ..

echo "✅ Release package created successfully!"
echo ""
echo "📁 Files created:"
echo "   - $RELEASE_DIR/anton-money-tracker.jar (Executable JAR)"
echo "   - $RELEASE_DIR/README.txt (Instructions)"
echo "   - $RELEASE_DIR/run.bat (Windows run script)"
echo "   - $RELEASE_DIR/run.sh (Linux/Mac run script)"
echo "   - anton-money-tracker-v1.0.0.zip (Complete package)"
echo ""
echo "🎉 Ready to upload to GitHub Releases!"
echo ""
echo "Next steps:"
echo "1. Test the JAR file: java -jar $RELEASE_DIR/anton-money-tracker.jar"
echo "2. Create a new release on GitHub"
echo "3. Upload the ZIP file or individual files"
