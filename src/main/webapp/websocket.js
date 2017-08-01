var ws;


function resetTextareaScrollPosition(){
    var textarea = document.getElementById("log");
    if(textarea.selectionStart == textarea.selectionEnd) {
        textarea.scrollTop = textarea.scrollHeight;
    }
}

function connect() {
    var username = document.getElementById("username").value;
    
    var host = document.location.host;
    var pathname = document.location.pathname;
    
    ws = new WebSocket("ws://" +host  + pathname + "chat/" + username);

    ws.onmessage = function(event) {
        var log = document.getElementById("log");
        console.log(event.data);
        var message = JSON.parse(event.data);
        log.innerHTML += message.from + " : " + message.content + "\n";
        
        resetTextareaScrollPosition();
    };
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