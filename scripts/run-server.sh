# scripts/run-server.sh
#!/bin/bash

echo "Starting CollabDraw Server..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java is not installed. Please install Java first."
    exit 1
fi

# Check if server JAR exists
if [ ! -f "build/server/collabdraw-server.jar" ]; then
    echo "Server JAR not found. Please build the project first."
    exit 1
fi

# Run server
java -jar build/server/collabdraw-server.jar