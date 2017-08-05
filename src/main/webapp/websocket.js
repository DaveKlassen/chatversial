var initiatedConnect = false;
var isConnected = false;
var heartBeatTimerId;
var userList = [];
var loginName;
var ws;

document.getElementById("username").focus();

function resetTextareaScrollPosition(){
    var textarea = document.getElementById("log");
    if(textarea.selectionStart == textarea.selectionEnd) {
        textarea.scrollTop = textarea.scrollHeight;
    }
}

function removeChatMessages() {
    var logEl = document.getElementById("log");
    while (logEl.hasChildNodes()) {
       logEl.removeChild(logEl.firstChild);
    }
}
function removeUsersList() {
    var listEl = document.getElementById("UserList");
    while (listEl.hasChildNodes()) {
       listEl.removeChild(listEl.firstChild);
    }
}
function setOffline() {

    var titleEl = document.getElementById("ListHeader");
    titleEl.removeChild(titleEl.firstChild);
    titleEl.innerHTML = "Offline";
}
function initiateDisconnect() {
    initiatedConnect = false;

    if (ws) {
        ws.close(1000, "Manual Disconnect");

        setOffline();
        removeUsersList();
        removeChatMessages();
    }
}

function connect() {

    if (!isConnected) {
        var username = document.getElementById("username").value;
        if ( (username) && (username !== "") ) {
            loginName = username;
            initiateConnection(username);
        } else {
            alert("Please enter a username");
        }
    } else {
        initiateDisconnect();
    }
}

function signalLiveConnection () {

    // Signal Live Connection
    isConnected = true;
    document.getElementById("connectButton").value = "Leave";        
    document.getElementById("msg").focus();
}

function signalDeadConnection () {

    // SignalConnectionLoss
    userList = [];
    isConnected = false;
    document.getElementById("connectButton").value = "Join";
    document.getElementById("username").focus();
}

function initiateConnection(username) {
    
    var host = document.location.host;
    var pathname = document.location.pathname;
    
    ws = new WebSocket("ws://" +host  + pathname + "chat/" + username);
    ws.onopen = function(event) {
        signalLiveConnection();

        var statusMsg = "Connected";
        var statusEl = document.getElementById("status");
        statusEl.innerHTML = statusMsg + " as " + username;

        // Only send this when the user manuall connects
        if (!initiatedConnect) {

            initiatedConnect = true;
            heartBeatTimerId = setInterval( function() {
                var json = JSON.stringify({
                    "to": "heartbeat",
                    "content": "alive " + userList.length
                });

                ws.send(json);
            }, 60000);
        }
    }
    ws.onerror = function(event) {
        var errorEl = document.getElementById("error");
        errorEl.innerHTML = "Encountered Error";
        console.log(event);
    }    
    ws.onmessage = function(event) {

        console.log(event.data);
        var message = JSON.parse(event.data);
        if (message.to) {

            if (message.to === "heartbeat") {
                console.log("Sever is alive: " + message.content);
            } else if (message.to === "addUser") {

                // We should actual filter users based on topic...
                removeUsersList();
                var listEl = document.getElementById("UserList");
                for (i in userList) {
                    listEl.innerHTML += userList[i] + "<br/>";
                }

                // Make the new username bold.
                listEl.innerHTML += "<b>" + message.content + "</b><br/>";
                userList.push(message.content);

                // Alter the display to signal a new user has joined a topic.
                var logEl = document.getElementById("log");
                logEl.innerHTML += message.content + " has joined " + message.from + "\n";
            } else if (message.to === "removeUser") {

                // We should actual filter users based on topic...
                removeUsersList();
                var listEl = document.getElementById("UserList");
                for (i in userList) {
                    if (userList[i] === message.content) {
                        userList.splice(i, 1);
                    } else {
                        listEl.innerHTML += userList[i] + "<br/>";
                    }
                }

                // Alter the display to signal a new user has joined a topic.
                var logEl = document.getElementById("log");
                logEl.innerHTML += message.content + " has left topic: " + message.from + "\n";
            } else if (message.to === "userList") {
                document.getElementById("ListHeader").innerHTML = "Online";

                removeUsersList();
                var listEl = document.getElementById("UserList");
                var users = message.content.split("\n");
                for (i in users) {

                    if (users[i] !== "") {
                        listEl.innerHTML += users[i] + "<br/>";
                        userList.push(users[i]);
                    }
                }
            } else if (message.to === "topic") {
                document.getElementById("TopicHeader").innerHTML = "Topic: ";
                document.getElementById("TopicName").innerHTML = message.content;
            }            
        } else if (message.from) {
            var logEl = document.getElementById("log");
            logEl.innerHTML += message.from + " : " + message.content + "\n";
        }
        
        resetTextareaScrollPosition();
    };
    ws.onclose = function(event) {

        // Remove the heartbeat message.
        clearInterval(heartBeatTimerId);

        // If we recieve the disconnect from the server...
        setOffline();
        removeUsersList();
        signalDeadConnection();

        var statusEl = document.getElementById("status");
        statusEl.innerHTML = "Closed";
    }
}

document.getElementById("username").addEventListener("keyup", function(e){
    if (e.keyCode === 13) {
        // Connect to the chat service
        connect();
    }
});


function send() {
    var content = document.getElementById("msg").value;
    var json = JSON.stringify({
        "content":content
    });

    ws.send(json);
    
    document.getElementById("msg").value = "";
    console.log(content);
}

document.getElementById("msg").addEventListener("keyup", function(e){
    if (e.keyCode === 13) {
        // Send the message
        send();
    }
});