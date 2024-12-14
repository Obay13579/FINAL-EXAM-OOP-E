const express = require('express');
const app = express();
const http = require('http').createServer(app);
const io = require('socket.io')(http);

// Serve static files from public directory
app.use(express.static('public'));

// Store connected users
let users = new Set();
let games = {};

io.on('connection', (socket) => {
    console.log('A user connected');

    socket.on('join', (username) => {
        users.add(username);
        socket.username = username;
        io.emit('userJoined', username);
        io.emit('userList', Array.from(users));
    });

    socket.on('chat message', (msg) => {
        io.emit('chat message', {
            username: socket.username,
            message: msg
        });
    });

    // Game invitation logic
    socket.on('invite-game', (opponent) => {
        const inviter = socket.username;
        io.emit('game-invitation', { inviter, opponent });
    });

    socket.on('accept-game', (inviter) => {
        const gameId = `game_${inviter}_${socket.username}`;
        games[gameId] = {
            players: [inviter, socket.username],
            playerSymbols: {
                [inviter]: 'O', 
                [socket.username]: 'X'
            },
            board: Array(9).fill(null),
            currentPlayer: inviter,
            winner: null
        };
        io.emit('start-game', { 
            gameId, 
            players: [inviter, socket.username],
            playerSymbols: games[gameId].playerSymbols,
            currentPlayer: inviter
        });
    });

    socket.on('game-move', (data) => {
        const { gameId, index, player } = data;
        if (games[gameId] && games[gameId].board[index] === null) {
            const playerSymbol = games[gameId].playerSymbols[player];
            games[gameId].board[index] = playerSymbol;
            const winner = checkWinner(games[gameId].board);
            
            if (winner) {
                games[gameId].winner = winner;
                const players = games[gameId].players;
                const loser = players.find(p => games[gameId].playerSymbols[p] !== winner);
                
                io.emit('game-over', { 
                    gameId, 
                    winner: winner === 'Draw' ? 'Draw' : games[gameId].playerSymbols[winner],
                    players: {
                        winner: winner === 'Draw' ? null : winner,
                        loser: winner === 'Draw' ? null : loser
                    },
                    winnerSymbol: playerSymbol, // Add the winning symbol
                    winningMove: index // Add the index of the winning move
                });
                delete games[gameId];
            } else {
                const currentPlayerIndex = games[gameId].players.indexOf(player);
                const nextPlayerIndex = (currentPlayerIndex + 1) % 2;
                games[gameId].currentPlayer = games[gameId].players[nextPlayerIndex];
                
                io.emit('update-game', {
                    gameId,
                    board: games[gameId].board,
                    currentPlayer: games[gameId].currentPlayer
                });
            }
        }
    });

    socket.on('disconnect', () => {
        if (socket.username) {
            users.delete(socket.username);
            io.emit('userLeft', socket.username);
            io.emit('userList', Array.from(users));
        }
        console.log('A user disconnected');
    });
});

function checkWinner(board) {
    const winPatterns = [
        [0, 1, 2], [3, 4, 5], [6, 7, 8], // Rows
        [0, 3, 6], [1, 4, 7], [2, 5, 8], // Columns
        [0, 4, 8], [2, 4, 6] // Diagonals
    ];

    for (let pattern of winPatterns) {
        const [a, b, c] = pattern;
        if (board[a] && board[a] === board[b] && board[a] === board[c]) {
            return board[a];
        }
    }

    return board.every(cell => cell !== null) ? 'Draw' : null;
}

const PORT = process.env.PORT || 3000;
http.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});