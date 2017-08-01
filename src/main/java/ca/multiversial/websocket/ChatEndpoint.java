package ca.multiversial.websocket;

import java.io.IOException;
import java.util.Map;
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
    private static ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> scores = new ConcurrentHashMap<>();
    private WebSocketSession webSocketSession;
    private JmsEndpoint jmsEndpoint = null;
    
    public static Map<String, Integer> getScores() {
    	Map<String, Integer> copyOfScores = new ConcurrentHashMap<>(scores);
    	return(copyOfScores);
    }
    
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException, EncodeException {

    	// Create a Jms client for the web socket to send/receive from.
        this.webSocketSession = new WebSocketSession(session);
    	this.jmsEndpoint = new JmsEndpoint(username, this.webSocketSession);
    	users.put(session.getId(), username);
        
        // Let everyone know who's here
        ChatMessage message = new ChatMessage();
        message.setFrom(username);
        message.setContent("Connected!");
        jmsEndpoint.send(message);
    }

    private boolean isJoinRequest(String content) {
    	boolean join = false;
    	
    	String trimmed = content.trim();
    	join = trimmed.startsWith("/Join ");
    	
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
        String topic = message.getContent().replaceFirst("/Join ", "");
        String topicName = topic.trim();
        boolean newTopic = false;
        if (null == currentTopics.get(topicName)) {
        	newTopic = true;
        	currentTopics.put(topicName, "");
        }
        jmsEndpoint = new JmsEndpoint(message.getFrom(), this.webSocketSession, topicName, newTopic);
    }    
    
    @OnMessage
    public void onMessage(Session session, ChatMessage message) throws IOException, EncodeException {
    	String userId = users.get(session.getId());
        message.setFrom(userId);
        
        if (isJoinRequest(message.getContent()) ) {
        	processJoinRequest(message);        	
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

    	// Send everyone a disconnect notice and close.
        ChatMessage message = new ChatMessage();
        message.setFrom(users.get(session.getId()));
        message.setContent("Disconnected!");
        jmsEndpoint.sendAndClose(message);        
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }
}
