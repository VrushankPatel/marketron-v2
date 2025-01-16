#!/bin/bash

# Create main project directories
mkdir -p tradar/{core,order-entry,matching-engine,reporting,reference-data,market-data}/src/{main,test}/{java,resources}

# Create package structure
BASE_PACKAGE="com/tradar"
for module in core order-entry matching-engine reporting reference-data market-data; do
    mkdir -p tradar/$module/src/main/java/$BASE_PACKAGE/$module
    mkdir -p tradar/$module/src/test/java/$BASE_PACKAGE/$module
done

# Make scripts executable
chmod +x scripts/build.sh scripts/run.sh 