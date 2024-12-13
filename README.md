# CollabDraw

A collaborative drawing application with real-time chat functionality, built using Java and Swing.

## Features

- Real-time collaborative drawing
- Chat functionality
- Multiple drawing tools
- Color picker
- Stroke size adjustment
- Save/load drawings
- User authentication
- Persistent chat history

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/collabdraw.git
cd collabdraw
```

2. Set up the database:
```bash
mysql -u root -p < docs/database/schema.sql
```

3. Configure the application:
- Edit `config/server.properties` for server settings
- Edit `config/client.properties` for client settings

4. Build the project:
```bash
./scripts/build.sh
```

## Running the Application

1. Start the server:
```bash
./scripts/run-server.sh
```

2. Start the client:
```bash
./scripts/run-client.sh
```

## Project Structure

```
CollabDraw/
├── src/                    # Source files
├── config/                 # Configuration files
├── docs/                   # Documentation
├── scripts/                # Build and run scripts
└── build/                  # Compiled files
```

## Development

- Use `mvn test` to run tests
- Use `mvn javadoc:javadoc` to generate documentation

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.