#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get the script directory and project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Function to print colored messages
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check dependencies
check_dependencies() {
    local missing=0
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_message $RED "Java is not installed"
        missing=1
    else
        local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$java_version" -lt "17" ]; then
            print_message $RED "Java 17 or higher is required"
            missing=1
        fi
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_message $RED "Maven is not installed"
        missing=1
    fi
    
    return $missing
}

# Function to setup development environment
setup_dev() {
    print_message $YELLOW "Setting up development environment..."
    
    # Change to project root directory
    cd "$PROJECT_ROOT"
    
    # Create necessary directories
    mkdir -p logs data
    
    # Install dependencies
    mvn dependency:resolve
    
    if [ $? -eq 0 ]; then
        print_message $GREEN "Development environment setup completed"
    else
        print_message $RED "Failed to setup development environment"
        exit 1
    fi
}

# Function to run tests
run_tests() {
    local module=$1
    
    # Change to project root directory
    cd "$PROJECT_ROOT"
    
    if [ -z "$module" ]; then
        print_message $YELLOW "Running all tests..."
        mvn test
    else
        print_message $YELLOW "Running tests for $module..."
        mvn -pl $module test
    fi
}

# Usage information
usage() {
    echo "Usage: $0 <command> [options]"
    echo "Commands:"
    echo "  setup     - Setup development environment"
    echo "  test      - Run tests (all or specific module)"
    echo "  check     - Check development dependencies"
    echo "Options:"
    echo "  -m, --module <module>  - Specify module for test command"
}

# Main execution
case "$1" in
    "setup")
        check_dependencies && setup_dev
        ;;
    "test")
        shift
        module=""
        while [[ $# -gt 0 ]]; do
            case "$1" in
                -m|--module)
                    module="$2"
                    shift 2
                    ;;
                *)
                    print_message $RED "Unknown option: $1"
                    usage
                    exit 1
                    ;;
            esac
        done
        run_tests "$module"
        ;;
    "check")
        check_dependencies
        ;;
    *)
        usage
        exit 1
        ;;
esac 