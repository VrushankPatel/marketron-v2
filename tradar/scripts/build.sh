#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Get the project root directory (parent of scripts directory)
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

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

# Function to build a specific module
build_module() {
    local module=$1
    print_message $YELLOW "Building module: $module"
    cd "$PROJECT_ROOT"
    
    # Build the module
    mvn -pl $module clean install
    if [ $? -ne 0 ]; then
        print_message $RED "Failed to build $module"
        return 1
    fi
    
    # Copy dependencies
    print_message $YELLOW "Copying dependencies for $module..."
    mvn -pl $module dependency:copy-dependencies
    if [ $? -ne 0 ]; then
        print_message $RED "Failed to copy dependencies for $module"
        return 1
    fi
    
    print_message $GREEN "Successfully built $module and copied dependencies"
    return 0
}

# Main build function
main_build() {
    print_message $YELLOW "Building tradar Trading Platform"
    
    # First build the parent project
    print_message $YELLOW "Building parent project..."
    cd "$PROJECT_ROOT"
    mvn clean install -N
    if [ $? -ne 0 ]; then
        print_message $RED "Failed to build parent project"
        return 1
    fi
    
    # Then build core module
    build_module core || return 1
    
    # Build other modules in parallel
    modules=("order-entry" "matching-engine" "reporting" "reference-data" "market-data")
    pids=()
    
    for module in "${modules[@]}"; do
        build_module "$module" &
        pids+=($!)
    done
    
    # Wait for all builds to complete
    failed=0
    for pid in "${pids[@]}"; do
        wait $pid || let "failed+=1"
    done
    
    if [ "$failed" -eq 0 ]; then
        print_message $GREEN "All modules built successfully"
        return 0
    else
        print_message $RED "$failed module(s) failed to build"
        return 1
    fi
}

# Execute main build
main_build 