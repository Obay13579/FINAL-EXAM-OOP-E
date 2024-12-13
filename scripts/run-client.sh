# scripts/run-client.sh
#!/bin/bash

echo "Starting CollabDraw Client..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java is not installed. Please install Java first."
    exit 1
fi

# Check if client JAR exists
if [ ! -f "build/client/collabdraw-client.jar" ]; then
    echo "Client JAR not found. Please build the project first."
    exit 1
fi

# Run client
java -jar build/client/collabdraw-client.jar