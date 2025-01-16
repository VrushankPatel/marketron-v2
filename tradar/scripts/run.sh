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

# Function to check if Java is installed
check_java() {
    if ! command -v java &> /dev/null; then
        print_message $RED "Java is not installed"
        exit 1
    fi
}

# Function to create necessary directories
setup_directories() {
    mkdir -p logs
    mkdir -p data
}

# Function to start a component
start_component() {
    local component=$1
    local main_class=$2
    local log_file="logs/${component}.log"
    local base_dir=$(cd "$(dirname "$0")/.." && pwd)
    
    print_message $YELLOW "Starting $component..."
    
    # Build the classpath with absolute paths
    local jar_name="tradar-${component}-1.0-SNAPSHOT.jar"
    local classpath="${base_dir}/${component}/target/${jar_name}"
    
    # Add core JAR
    classpath="${classpath}:${base_dir}/core/target/tradar-core-1.0-SNAPSHOT.jar"
    
    # Add target/classes and all dependencies
    classpath="${classpath}:${base_dir}/${component}/target/classes"
    classpath="${classpath}:${base_dir}/core/target/classes"
    
    # Add all JARs from target/dependency
    if [ -d "${base_dir}/${component}/target/dependency" ]; then
        classpath="${classpath}:${base_dir}/${component}/target/dependency/*"
    fi
    if [ -d "${base_dir}/core/target/dependency" ]; then
        classpath="${classpath}:${base_dir}/core/target/dependency/*"
    fi
    
    # Print classpath for debugging
    echo "Using classpath: $classpath" >> "$log_file"
    echo "Main class: $main_class" >> "$log_file"
    echo "Working directory: ${base_dir}" >> "$log_file"
    
    # Start the component with absolute paths
    cd "${base_dir}"
    java -cp "$classpath" $main_class > "${base_dir}/scripts/$log_file" 2>&1 &
    local pid=$!
    cd "${base_dir}/scripts"
    
    sleep 2  # Give it a moment to start up and log errors
    
    # Check if process started successfully
    if ps -p $pid > /dev/null; then
        print_message $GREEN "$component started (PID: $pid)"
        echo $pid > "data/${component}.pid"
        return 0
    else
        print_message $RED "Failed to start $component"
        echo "=== Last 20 lines of log ===" >&2
        tail -n 20 "$log_file" >&2
        return 1
    fi
}

# Function to stop a component
stop_component() {
    local component=$1
    local pid_file="data/${component}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null; then
            print_message $YELLOW "Stopping $component (PID: $pid)..."
            kill $pid
            rm "$pid_file"
            print_message $GREEN "$component stopped"
        else
            print_message $YELLOW "$component is not running"
            rm "$pid_file"
        fi
    else
        print_message $YELLOW "$component is not running"
    fi
}

# Function to show component status
show_status() {
    local component=$1
    local pid_file="data/${component}.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null; then
            print_message $GREEN "$component is running (PID: $pid)"
        else
            print_message $RED "$component is not running (stale PID file)"
            rm "$pid_file"
        fi
    else
        print_message $RED "$component is not running"
    fi
}

# Function to show logs
show_logs() {
    local component=$1
    local log_file="logs/${component}.log"
    
    if [ -f "$log_file" ]; then
        tail -f "$log_file"
    else
        print_message $RED "No logs found for $component"
    fi
}

# Usage information
usage() {
    echo "Usage: $0 <command> [component]"
    echo "Commands:"
    echo "  start <component>  - Start a specific component"
    echo "  stop <component>   - Stop a specific component"
    echo "  restart <component>- Restart a specific component"
    echo "  status [component] - Show status of all or specific component"
    echo "  logs <component>   - Show logs for a specific component"
    echo "  start-all         - Start all components"
    echo "  stop-all          - Stop all components"
    echo ""
    echo "Components:"
    echo "  market-data       - Market Data Service"
    echo "  order-entry       - Order Entry Service"
    # echo "  matching-engine   - Matching Engine"
    echo "  reporting         - Reporting Service"
    # echo "  reference-data    - Reference Data Service"
}

# Component configurations
MAIN_CLASS_MARKET_DATA="com.tradar.marketdata.ui.MarketDataViewer"
MAIN_CLASS_ORDER_ENTRY="com.tradar.order.entry.ui.OrderEntryForm"
MAIN_CLASS_REPORTING="com.tradar.reporting.ui.ReportViewer"
# MAIN_CLASS_REFERENCE_DATA="com.tradar.refdata.ReferenceDataService"

# Function to get main class
get_main_class() {
    case "$1" in
        "market-data")     echo "$MAIN_CLASS_MARKET_DATA" ;;
        "order-entry")     echo "$MAIN_CLASS_ORDER_ENTRY" ;;
        # "matching-engine") echo "$MAIN_CLASS_MATCHING_ENGINE" ;;
        "reporting")       echo "$MAIN_CLASS_REPORTING" ;;
        # "reference-data")  echo "$MAIN_CLASS_REFERENCE_DATA" ;;
        *)                 echo "" ;;
    esac
}

# Function to get all components
get_all_components() {
    echo "market-data order-entry reporting"
    # echo "market-data order-entry matching-engine reporting reference-data"
}

# Main execution
check_java
setup_directories

case "$1" in
    "start")
        if [ -z "$2" ]; then
            print_message $RED "Component name required"
            usage
            exit 1
        fi
        main_class=$(get_main_class "$2")
        if [ -z "$main_class" ]; then
            print_message $RED "Invalid component: $2"
            usage
            exit 1
        fi
        start_component "$2" "$main_class"
        ;;
    "stop")
        if [ -z "$2" ]; then
            print_message $RED "Component name required"
            usage
            exit 1
        fi
        stop_component "$2"
        ;;
    "restart")
        if [ -z "$2" ]; then
            print_message $RED "Component name required"
            usage
            exit 1
        fi
        stop_component "$2"
        sleep 2
        main_class=$(get_main_class "$2")
        if [ -z "$main_class" ]; then
            print_message $RED "Invalid component: $2"
            usage
            exit 1
        fi
        start_component "$2" "$main_class"
        ;;
    "status")
        if [ -z "$2" ]; then
            for component in $(get_all_components); do
                show_status "$component"
            done
        else
            show_status "$2"
        fi
        ;;
    "logs")
        if [ -z "$2" ]; then
            print_message $RED "Component name required"
            usage
            exit 1
        fi
        show_logs "$2"
        ;;
    "start-all")
        for component in $(get_all_components); do
            main_class=$(get_main_class "$component")
            start_component "$component" "$main_class"
        done
        ;;
    "stop-all")
        for component in $(get_all_components); do
            stop_component "$component"
        done
        ;;
    *)
        usage
        exit 1
        ;;
esac 