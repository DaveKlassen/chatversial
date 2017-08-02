var initiatedConnect = false;
var isConnected = false;
var loginName;
var ws;

document.getElementById("username").focus();

function resetTextareaScrollPosition(){
    var textarea = document.getElementById("log");
    if(textarea.selectionStart == textarea.selectionEnd) {
        textarea.scrollTop = textarea.scrollHeight;
    }
}

function removeUsersList() {
    var listEl = document.getElementById("UserList");
    while (listEl.hasChildNodes()) {
       listEl.removeChild(listEl.firstChild);
    }  
}
function initiateDisconnect() {
    initiatedConnect = false;

    if (ws) {
        ws.close(1000, "Manual Disconnect");

        var titleEl = document.getElementById("ListHeader");
        titleEl.removeChild(titleEl.firstChild);
        titleEl.innerHTML = "Offline";
        removeUsersList();
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
            var json = JSON.stringify({
                "content": statusMsg
            });

            ws.send(json);
            initiatedConnect = true;
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
        if (message.from) {
            var logEl = document.getElementById("log");
            logEl.innerHTML += message.from + " : " + message.content + "\n";
        } else if (message.to) {

            if (message.to === "userList") {
                document.getElementById("ListHeader").innerHTML = "Online";

                removeUsersList();
                var listEl = document.getElementById("UserList");
                var userList = message.content.split("\n");                
                for (i in userList) {
                    listEl.innerHTML += userList[i] + "<br/>";
                }
            } else if (message.to === "topic") {
                document.getElementById("TopicHeader").innerHTML = "Topic: ";
                document.getElementById("TopicName").innerHTML = message.content;
            }            
        }
        
        resetTextareaScrollPosition();
    };
    ws.onclose = function(event) {
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
}

document.getElementById("msg").addEventListener("keyup", function(e){
    if (e.keyCode === 13) {
        // Send the message
        send();
    }
});