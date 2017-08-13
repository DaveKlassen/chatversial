var initiatedConnect = false;
var isConnected = false;
var prevChatInit = false;
var prevChatTextPos = 1;
var prevChatText = [];
var heartBeatTimerId;
var topicList = [];
var userList = [];
var loginName;
var ws;


function getXmlHttpRequestObject() {

    if (typeof(XMLHttpRequest) == "undefined")  {

        XMLHttpRequest = function() {
            try {
                return new ActiveXObject("Msxml2.XMLHTTP.6.0");
            } catch(e) {} try {
                return new ActiveXObject("Msxml2.XMLHTTP.3.0");
            } catch(e) {} try {
                return new ActiveXObject("Msxml2.XMLHTTP");
            } catch(e) {} try {
                return new ActiveXObject("Microsoft.XMLHTTP");
            } catch(e) {}
            alert("get a new browser");
            throw new Error("This browser does not support XMLHttpRequest.");
        };
    }

    var req = new XMLHttpRequest();
    return(req);
}

function processChatTopics(req) {

    // If the request state had data to process.
    if (req.readyState == 4) {

        // If the request status is 'OK'
        if (req.status == 200) {
            console.log(req.response);
            topicList = JSON.parse(req.response);

            // Make the first topic the default login entry
            var e = document.getElementById("TopicName");
            e.value = topicList[0];
        } else {

            // Display error status.
            alert("Error Status Code: " + req.status);
        }
    }
}

// Perform GET AJAX call to get current chat Topics
function getChatTopics() {
    var url = "/topics/?time=" + new Date().getTime();
    var req = getXmlHttpRequestObject();

    // Open the request
    req.open("GET", url, true);

    // Set the request processing function
    req.onreadystatechange=function() { processChatTopics(req) };

    // Send the Request
    req.send(null);
}

/* Add a hint below the Topic field for the user to select. */
var myAddHintFunction = function (email) {

    // Create the hint
    var y = document.createElement("div");
    var textnode = document.createTextNode(email);
    y.appendChild(textnode);
    y.className = "hint";

    // If the hint is clicked
    y.addEventListener("click", function(emailField) {

        // Change the topic input element
        var data = y.childNodes[0].nodeValue;
        var e = document.getElementById("TopicName");
        e.value = data;

        // Remove the drop down hints.
        var z = document.getElementById("HintList").parentElement;
        var x = document.getElementById("HintList");
        z.removeChild(x);
    });

    return(y);
};

function searchHints() {

    // Get the hint list if it exists
    var x = document.getElementById("HintList");
    if ( (x === undefined) || (x === null) ) {

        // Create the hint div
        var div = document.createElement("div");
        div.id = "HintList";

        for (var i = 0; i < topicList.length; i++) {

            // Add this contact to the hint list.
            var newHint = myAddHintFunction(topicList[i]);
            div.appendChild(newHint);
        }

        // Append it to the parent input field.
        var e = document.getElementById("TopicDropdown");
        e.appendChild(div);
    } else {
        // Remove the drop down hints.
        var z = document.getElementById("HintList").parentElement;
        z.removeChild(x);
    }
};

window.onload = function() {
    getChatTopics();
};
function addTopicToList(topic) {
    var found = false;
    for (var i = 0; i < topicList.length; i++) {
        if (topic === topicList[i]) {
            found = true;
        }
    }
    if (!found) {
        topicList.push(topic);
    }
}

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
    document.getElementById("TopicName").disabled = true;
    document.getElementById("TopicButton").onclick = function() {};
    document.getElementById("connectButton").value = "Leave";        
    document.getElementById("msg").focus();
}

function signalDeadConnection () {

    // SignalConnectionLoss
    userList = [];
    isConnected = false;
    document.getElementById("TopicName").disabled = false;
    document.getElementById("TopicButton").onclick = searchHints;
    document.getElementById("connectButton").value = "Join";
    document.getElementById("username").focus();
}

function initiateConnection(username) {
    
    var host = document.location.host;
    var pathname = document.location.pathname;
    var topic = document.getElementById("TopicName").value;
    
    ws = new WebSocket("ws://" +host  + pathname + "chat/" + topic + "/" + username);
    ws.onopen = function(event) {
        signalLiveConnection();

        var statusMsg = "Connected";
        var statusEl = document.getElementById("status");
        statusEl.innerHTML = statusMsg + " as " + username;

        // Only send this when the user manually connects
        if (!initiatedConnect) {

            initiatedConnect = true;

            // Start a heartbeat time to keep the connection alive.
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

                // If we have joined a new topic, clear the chat log.
                removeChatMessages();

                // Set the topic and add the new topic to our list.
                var e = document.getElementById("TopicName");
                e.value = message.content;
                addTopicToList(message.content);
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


function send(content) {
    var json = JSON.stringify({
        "content":content
    });

    // Send the text and add it to our counter.
    ws.send(json);

    // Store in previous texts buffer history.
    if (!prevChatInit) {
        prevChatText.push(content);
    } else {

        // If buffer history initialized replace what was last added.
        var size = prevChatText.length;
        prevChatText[size - 1] = content;
    }
    prevChatTextPos = 1;
    prevChatInit = false;
    
    document.getElementById("msg").value = "";
    console.log(content);
}

document.getElementById("msg").addEventListener("keyup", function(e){

    if (e.keyCode === 13) {
        var content = document.getElementById("msg").value;

        // We cannot send an empty text.
        if (content !== "") {

            // Send the message
            send(content);
        }
    } else if (e.keyCode === 38) {

        // On up arrow show previous chat entries
        if ( prevChatText.length > 0) {            

            // If not initialized add the current msg text
            if (!prevChatInit) { 
                prevChatText.push(document.getElementById("msg").value);
                prevChatInit = true;
            } else if (1 === prevChatTextPos)  {

                // If initialized and at position 1, overwrite the current value.
                prevChatText[prevChatText.length - 1] = document.getElementById("msg").value;
            }            
            var size = prevChatText.length;

            // While the length of buffer is within the count range
            if (size > prevChatTextPos) {
                document.getElementById("msg").value = prevChatText[size - ++prevChatTextPos];
            } else if (size === prevChatTextPos) {
                document.getElementById("msg").value = prevChatText[size - prevChatTextPos];
            }
        }
    } else if (e.keyCode === 40) {

        // On down arrow advance to the more recent chat entries
        if ( prevChatText.length > 0) {
            var size = prevChatText.length;

            // While the length of buffer is within the count range
            if (1 < prevChatTextPos) {
                document.getElementById("msg").value = prevChatText[size - --prevChatTextPos];
            }
        }        
    }
});