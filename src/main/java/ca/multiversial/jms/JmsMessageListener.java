package ca.multiversial.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import ca.multiversial.model.ChatMessage;
import ca.multiversial.websocket.WebSocketSession;

public class JmsMessageListener implements MessageListener {
	private WebSocketSession webSocketSession;
	private String consumerName;
	
	public JmsMessageListener(String consumerName, WebSocketSession webSocketSession) {
		this.consumerName = consumerName;
		this.webSocketSession = webSocketSession;
	}

	public void onMessage(Message message) {
		ObjectMessage objMessage = (ObjectMessage) message;
		try {
			//System.out.println(consumerName + " received " + objMessage.toString());

			// Cast the Jms Message back to Chat format.
			ChatMessage msg = (ChatMessage) objMessage.getObject();
			this.webSocketSession.send(msg);
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
}