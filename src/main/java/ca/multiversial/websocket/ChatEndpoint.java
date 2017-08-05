package ca.multiversial.websocket;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import ca.multiversial.jms.JmsEndpoint;
import ca.multiversial.model.ChatMessage;


@ServerEndpoint(value = "/chat/{username}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class ChatEndpoint {
    private static ConcurrentHashMap<String, String> currentTopics = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> scores = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();
    private static final Set<ChatEndpoint> chatEndpoints = new CopyOnWriteArraySet<>();
    private WebSocketSession webSocketSession;
    private JmsEndpoint jmsEndpoint = null;
    
    public static Map<String, Integer> getScores() {
        Map<String, Integer> copyOfScores = new ConcurrentHashMap<>(scores);
        return(copyOfScores);
    }
    
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException, EncodeException {

        // Tell everyone we have a new user joining
        broadcastUserAdded(username, "defaultTopic");

        // Create a Jms client for the web socket to send/receive from.
        this.webSocketSession = new WebSocketSession(session);
        this.jmsEndpoint = new JmsEndpoint(username, this.webSocketSession);
        users.put(session.getId(), username);
        chatEndpoints.add(this);

        sendTopic("defaultTopic");
        sendUserList();
    }

    private void broadcastUserAdded(String username, String topic) {

        // Send the list of connected users to the client.
        ChatMessage message = new ChatMessage();
        message.setFrom(topic);
        message.setTo("addUser");
        message.setContent(username);
        broadcast(message);
    }
    private void broadcastUserRemoved(String username, String topic) {

        // Send the list of connected users to the client.
        ChatMessage message = new ChatMessage();
        message.setFrom(topic);
        message.setTo("removeUser");
        message.setContent(username);
        broadcast(message);
    }
    private void sendTopic(String topicName) {

        // Send the list of connected users to the client.
        ChatMessage message = new ChatMessage();
        message.setTo("topic");
        message.setContent(topicName);
        webSocketSession.send(message);
    }
    private void sendUserList() {

        // Send the list of connected users to the client.
        ChatMessage message = new ChatMessage();
        message.setTo("userList");

        String theList = "";
        for (Enumeration<String> e = users.elements(); e.hasMoreElements();) {
            String username = e.nextElement();
            theList += username + "\n";
            System.out.println(username);
        }
        message.setContent(theList);

        // We need to broadcast this to update all users.
        webSocketSession.send(message);
    }

    private boolean isJoinRequest(String content) {
        boolean join = false;
        
        String trimmed = content.trim();
        join = trimmed.matches("(?i:[\\s]?/Join[\\s]?.*)");

        return(join);
    }

    private void processJoinRequest(ChatMessage message) {
        
        // Send everyone a disconnect notice and close this Jms session.
        ChatMessage byeMessage = new ChatMessage();
        byeMessage.setFrom(message.getFrom() );
        byeMessage.setContent("Disconnected!");
        this.jmsEndpoint.sendAndClose(byeMessage);
        this.jmsEndpoint = null;

        // Create the new topic,
        String topic = message.getContent().replaceFirst("(?i:[\\s]?/Join[\\s]?)", "");
        System.out.println(topic);

        String topicName = topic.trim();
        boolean newTopic = false;
        if (null == currentTopics.get(topicName)) {
            newTopic = true;
            currentTopics.put(topicName, "");
        }
        jmsEndpoint = new JmsEndpoint(message.getFrom(), this.webSocketSession, topicName, newTopic);
        sendTopic(topicName);
    }
    
    @OnMessage
    public void onMessage(Session session, ChatMessage message) throws IOException, EncodeException {
        String userId = users.get(session.getId());
        message.setFrom(userId);
        String to = message.getTo();
        
        if (isJoinRequest(message.getContent()) ) {
            processJoinRequest(message);            
        } else if ( (to != null) && (to.equals("heartbeat")) ) {
            message.setFrom("Admin");
            message.setContent("Alive " + users.size());
            webSocketSession.send(message);
        } else {

            jmsEndpoint.send(message);
            
            // Score the message
            int score = message.getContent().length();
            if (null == scores.get(userId) ) {
                scores.put(userId, score);
                System.out.println(userId + " First score: " + score);
            } else {
                score += scores.get(userId);
                scores.put(userId, score);
                System.out.println(userId + " score: " + score);
            }
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {

        // Remove user from lists
        String username = users.get(session.getId());
        jmsEndpoint.close();
        chatEndpoints.remove(this);
        users.remove(session.getId() );

        // Tell everyone else they left.
        broadcastUserRemoved(username, "notSure");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private static void broadcast(ChatMessage message) {
        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                endpoint.webSocketSession.send(message);
            }
        });
    }    
}
