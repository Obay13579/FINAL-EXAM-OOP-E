#!/bin/bash

# Stop on any error
set -e

echo "Building CollabDraw..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Clean and package with Maven
mvn clean package

# Create directories if they don't exist
mkdir -p build/server
mkdir -p build/client

# Copy server JAR
cp target/collabdraw-1.0-SNAPSHOT-jar-with-dependencies.jar build/server/collabdraw-server.jar

# Copy client JAR
cp target/collabdraw-1.0-SNAPSHOT-jar-with-dependencies.jar build/client/collabdraw-client.jar

# Copy configuration files
cp config/server.properties build/server/
cp config/client.properties build/client/

# Make run scripts executable
chmod +x scripts/run-server.sh
chmod +x scripts/run-client.sh

echo "Build completed successfully!"
echo "Server JAR: build/server/collabdraw-server.jar"
echo "Client JAR: build/client/collabdraw-client.jar"