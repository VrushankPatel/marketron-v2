#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Clean Maven build artifacts
print_message $YELLOW "Cleaning Maven build artifacts..."
mvn clean
if [ $? -eq 0 ]; then
    print_message $GREEN "Maven clean completed successfully"
else
    print_message $RED "Maven clean failed"
fi

# Clean logs and runtime data
print_message $YELLOW "Cleaning logs and runtime data..."
rm -rf logs/*
rm -rf data/*

# Clean IDE-specific files
print_message $YELLOW "Cleaning IDE files..."
find . -name "*.iml" -type f -delete
find . -name ".idea" -type d -exec rm -rf {} +
find . -name "*.class" -type f -delete
find . -name "target" -type d -exec rm -rf {} +
find . -name ".project" -type f -delete
find . -name ".classpath" -type f -delete
find . -name ".settings" -type d -exec rm -rf {} +

print_message $GREEN "Cleanup completed" 