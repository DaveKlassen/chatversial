package ca.multiversial.websocket;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import ca.multiversial.model.ChatMessage;

public class WebSocketSession {
    private Session session;
    
    public Session getSession() {
        return session;
    }

    public WebSocketSession(Session session) {
        this.session = session;
    }
    
    public void send(ChatMessage message) {
        try {
            
            // If the session is open send it.
            if (session.isOpen() ) {
                session.getBasicRemote().sendObject(message);
            }
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }
}
