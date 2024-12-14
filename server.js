const express = require('express');
const app = express();
const http = require('http').createServer(app);
const io = require('socket.io')(http);

// Serve static files from public directory
app.use(express.static('public'));

// Store connected users
let users = new Set();

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

    socket.on('disconnect', () => {
        if (socket.username) {
            users.delete(socket.username);
            io.emit('userLeft', socket.username);
            io.emit('userList', Array.from(users));
        }
        console.log('A user disconnected');
    });
});

const PORT = process.env.PORT || 3000;
http.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});