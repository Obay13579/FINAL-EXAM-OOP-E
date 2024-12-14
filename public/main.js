const socket = io();

function joinChat() {
    const username = document.getElementById('username').value.trim();
    if (username) {
        socket.emit('join', username);
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
        const li = document.createElement('li');
        li.textContent = user;
        usersList.appendChild(li);
    });
});