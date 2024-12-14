const socket = io();
let currentGame = null;
let currentUsername = null;

function joinChat() {
    const username = document.getElementById('username').value.trim();
    if (username) {
        socket.emit('join', username);
        currentUsername = username;
        document.getElementById('login-screen').style.display = 'none';
        document.getElementById('chat-screen').style.display = 'block';
    }
}

function sendMessage() {
    const input = document.getElementById('message-input');
    const message = input.value.trim();
    if (message) {
        socket.emit('chat message', message);
        input.value = '';
    }
}

// Enter key to send message
document.getElementById('message-input')?.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        sendMessage();
    }
});

// Existing socket event handlers
socket.on('chat message', (data) => {
    const messages = document.getElementById('messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message';
    messageDiv.textContent = `${data.username}: ${data.message}`;
    messages.appendChild(messageDiv);
    messages.scrollTop = messages.scrollHeight;
});

socket.on('userJoined', (username) => {
    const messages = document.getElementById('messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'system-message';
    messageDiv.textContent = `${username} has joined the chat`;
    messages.appendChild(messageDiv);
});

socket.on('userLeft', (username) => {
    const messages = document.getElementById('messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'system-message';
    messageDiv.textContent = `${username} has left the chat`;
    messages.appendChild(messageDiv);
});

socket.on('userList', (users) => {
    const usersList = document.getElementById('users');
    usersList.innerHTML = '';
    users.forEach(user => {
        // Don't show invite button for the current user
        if (user !== currentUsername) {
            const li = document.createElement('li');
            li.textContent = user;
            
            // Create invite button for each user
            const inviteBtn = document.createElement('button');
            inviteBtn.textContent = 'Invite to Game';
            inviteBtn.classList.add('game-invite-btn');
            inviteBtn.onclick = () => inviteToGame(user);
            
            li.appendChild(inviteBtn);
            usersList.appendChild(li);
        }
    });
});

// New game-related functions
function inviteToGame(opponent) {
    socket.emit('invite-game', opponent);
}

socket.on('game-invitation', (data) => {
    // Only show invitation to the invited user
    if (data.opponent === currentUsername) {
        const messages = document.getElementById('messages');
        const inviteDiv = document.createElement('div');
        inviteDiv.className = 'system-message game-invite';
        inviteDiv.innerHTML = `
            ${data.inviter} invites you to play Tic Tac Toe! 
            <button onclick="acceptGame('${data.inviter}')">Accept</button>
        `;
        messages.appendChild(inviteDiv);
        messages.scrollTop = messages.scrollHeight;
    }
});

function acceptGame(inviter) {
    socket.emit('accept-game', inviter);
    // Remove game invite message
    const inviteMessages = document.querySelectorAll('.game-invite');
    inviteMessages.forEach(msg => msg.remove());
}

socket.on('start-game', (gameData) => {
    currentGame = gameData;
    document.getElementById('game-area').style.display = 'block';
    document.getElementById('game-status').textContent = `Game between ${gameData.players[0]} and ${gameData.players[1]}`;
    document.getElementById('player-symbol').textContent = `Your symbol: ${gameData.playerSymbols[currentUsername]}`;
    resetBoard();
});

// Board interaction
const cells = document.querySelectorAll('.cell');
cells.forEach(cell => {
    cell.addEventListener('click', () => handleCellClick(cell));
});

function handleCellClick(cell) {
    const index = cell.getAttribute('data-index');
    
    // Only allow move if it's the current user's turn and the cell is empty
    if (currentGame && 
        currentUsername === currentGame.currentPlayer && 
        !cell.textContent) {
        socket.emit('game-move', {
            gameId: currentGame.gameId,
            index: parseInt(index),
            player: currentUsername
        });
    }
}

socket.on('update-game', (gameData) => {
    if (currentGame && gameData.gameId === currentGame.gameId) {
        currentGame = { ...currentGame, ...gameData };
        updateBoard(gameData.board);
        document.getElementById('game-status').textContent = `Current Turn: ${gameData.currentPlayer}`;
    }
});

socket.on('game-over', (gameData) => {
    if (currentGame && gameData.gameId === currentGame.gameId) {
        const messages = document.getElementById('messages');
        const gameOverDiv = document.createElement('div');
        gameOverDiv.className = 'system-message game-over';
        
        if (gameData.winner === 'Draw') {
            gameOverDiv.textContent = 'Game is a Draw! Both players played well.';
            document.getElementById('game-status').textContent = 'Game is a Draw!';
        } else {
            const isCurrentUserWinner = gameData.players.winner === currentUsername;
            gameOverDiv.textContent = isCurrentUserWinner 
                ? 'Congratulations! You won the game!' 
                : `${gameData.players.winner} won the game!`;
            
            document.getElementById('game-status').textContent = isCurrentUserWinner 
                ? 'You won!' 
                : `${gameData.players.winner} wins!`;
        }
        
        messages.appendChild(gameOverDiv);
        messages.scrollTop = messages.scrollHeight;
        
        setTimeout(() => {
            document.getElementById('game-area').style.display = 'none';
            currentGame = null;
        }, 3000);
    }
});

function updateBoard(board) {
    cells.forEach((cell, index) => {
        cell.textContent = board[index] || '';
    });
}

function resetBoard() {
    cells.forEach(cell => {
        cell.textContent = '';
    });
}