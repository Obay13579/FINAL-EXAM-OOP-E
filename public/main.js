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
        // Don't show button for the current user
        if (user !== currentUsername) {
            const li = document.createElement('li');
            
            // Create interactive button for each user
            const userBtn = document.createElement('button');
            userBtn.textContent = user;
            userBtn.classList.add('user-list-btn');
            
            // Hover states
            userBtn.addEventListener('mouseenter', (e) => {
                if (!userBtn.disabled) {
                    userBtn.textContent = 'Invite to Game';
                    userBtn.classList.add('invite-hover');
                }
            });
            
            userBtn.addEventListener('mouseleave', (e) => {
                if (!userBtn.disabled) {
                    userBtn.textContent = user;
                    userBtn.classList.remove('invite-hover');
                }
            });
            
            // Click to invite
            userBtn.addEventListener('click', () => {
                const originalText = user;
                
                // Disable the button and change text
                userBtn.textContent = 'Sending invite...';
                userBtn.disabled = true;
                
                // Emit the invite
                socket.emit('invite-game', user);
                
                // Reset button after 3 seconds
                setTimeout(() => {
                    userBtn.textContent = originalText;
                    userBtn.disabled = false;
                }, 3000);
            });
            
            li.appendChild(userBtn);
            usersList.appendChild(li);
        }
    });
});

// New game-related functions
function inviteToGame(opponent) {
    const inviteBtn = event.target;
    const originalText = inviteBtn.textContent;
    
    // Disable the button and change text
    inviteBtn.textContent = 'Sending invite...';
    inviteBtn.disabled = true;
    
    // Emit the invite
    socket.emit('invite-game', opponent);
    
    // Reset button after 3 seconds
    setTimeout(() => {
        inviteBtn.textContent = originalText;
        inviteBtn.disabled = false;
    }, 3000);
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
    
    // Check if current user is a player
    const isPlayer = gameData.players.includes(currentUsername);
    
    if (isPlayer) {
        const initialStatus = gameData.currentPlayer === currentUsername 
            ? 'Current Turn: **You**' 
            : `Current Turn: ${gameData.currentPlayer}`;
        document.getElementById('game-status').innerHTML = initialStatus.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        document.getElementById('player-symbol').textContent = `Your symbol: ${gameData.playerSymbols[currentUsername]}`;
    } else {
        // Spectator view
        document.getElementById('game-status').textContent = `Current Turn: ${gameData.currentPlayer}`;
        document.getElementById('player-symbol').textContent = `Spectating`;
    }
    resetBoard();
});

// Board interaction
const cells = document.querySelectorAll('.cell');
cells.forEach(cell => {
    cell.addEventListener('click', () => handleCellClick(cell));
});

function handleCellClick(cell) {
    const index = cell.getAttribute('data-index');
    
    // Only allow move if it's a player's turn and the cell is empty
    if (currentGame && 
        currentGame.players.includes(currentUsername) && // Check if user is a player
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
        
        // Check if current user is a player in the game
        const isPlayer = currentGame.players.includes(currentUsername);
        
        if (isPlayer) {
            // Update turn display based on current player
            const statusText = gameData.currentPlayer === currentUsername 
                ? 'Current Turn: **You**' 
                : `Current Turn: ${gameData.currentPlayer}`;
            document.getElementById('game-status').innerHTML = statusText.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        } else {
            // Spectator view
            document.getElementById('game-status').textContent = `Current Turn: ${gameData.currentPlayer}`;
        }
    }
});

function resetBoard() {
    cells.forEach(cell => {
        cell.textContent = ''; // Clear cell text
        cell.classList.remove('winning-cell'); // Remove winning cell highlight
    });
}

socket.on('game-over', (gameData) => {
    if (currentGame && gameData.gameId === currentGame.gameId) {
        const finalBoard = [...currentGame.board];
        if (gameData.winningMove !== undefined) {
            finalBoard[gameData.winningMove] = gameData.winnerSymbol;
        }
        
        updateBoard(finalBoard);
        
        const messages = document.getElementById('messages');
        const gameOverDiv = document.createElement('div');
        gameOverDiv.className = 'system-message game-over';
        
        // Check if current user is a player
        const isPlayer = currentGame.players.includes(currentUsername);
        
        if (gameData.winner === 'Draw') {
            gameOverDiv.textContent = 'Game is a Draw! Both players played well.';
            document.getElementById('game-status').textContent = 'Game is a Draw!';
        } else {
            const isCurrentUserWinner = gameData.players.winner === currentUsername;
            
            if (gameData.winningMove !== undefined) {
                const winningCell = document.querySelector(`.cell[data-index="${gameData.winningMove}"]`);
                winningCell.classList.add('winning-cell');
            }
            
            if (isPlayer) {
                gameOverDiv.textContent = isCurrentUserWinner 
                    ? 'Congratulations! You won the game!' 
                    : `${gameData.players.winner} won the game!`;
                
                document.getElementById('game-status').innerHTML = isCurrentUserWinner 
                    ? '<strong>You won!</strong>' 
                    : `${gameData.players.winner} wins!`;
            } else {
                // Spectator view
                gameOverDiv.textContent = `${gameData.players.winner} won the game!`;
                document.getElementById('game-status').textContent = `${gameData.players.winner} wins!`;
            }
        }
        
        messages.appendChild(gameOverDiv);
        messages.scrollTop = messages.scrollHeight;
        
        setTimeout(() => {
            document.getElementById('game-area').style.display = 'none';
            resetBoard();
            currentGame = null;
            
            document.querySelectorAll('.cell').forEach(cell => {
                cell.classList.remove('winning-cell');
            });
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