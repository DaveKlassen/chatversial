package ca.multiversial.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import ca.multiversial.model.ChatMessage;
import ca.multiversial.websocket.WebSocketSession;


public class JmsEndpoint {
    //private JmsSession jmsSession = JmsSession.getInstance();
    private MessageConsumer consumer = null;
    private MessageProducer publisher = null;
    private Topic topic = null;
    
    public JmsEndpoint(String userName, WebSocketSession webSocketSession) {

        try {
            Session session = JmsSession.getSession();
            topic = JmsSession.getTopic();
            
            // Create consumer and publisher handles.
            userName = userName + System.currentTimeMillis();
            consumer = session.createConsumer(JmsSession.getTopic() );
            consumer.setMessageListener(new JmsMessageListener(userName, webSocketSession));
            publisher = session.createProducer(topic);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public JmsEndpoint(String userName, WebSocketSession webSocketSession, String topicName, boolean first) {

        try {
            Session session = JmsSession.getSession();
            topic = session.createTopic(topicName + "?consumer.retroactive=true");
            
            // Create consumer and publisher handles.
            String handleName = userName + System.currentTimeMillis();
            consumer = session.createConsumer(topic);
            consumer.setMessageListener(new JmsMessageListener(handleName, webSocketSession));
            publisher = session.createProducer(topic);

            // Send a connection message
            ChatMessage message = new ChatMessage();
            message.setFrom(userName);
            if (first) {
                // Send the first message for this topic
                message.setContent("Welcomes you to: " + topicName);                
            } else {
                message.setContent("has joined: " + topicName);
            }
            Message objMsg = session.createObjectMessage(message);  
            publisher.send(objMsg, javax.jms.DeliveryMode.PERSISTENT, javax.jms.Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
            
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    public void send(ChatMessage chatMessage) {

        System.out.println("Sending text '" + chatMessage.getContent() + "'");
        try {
            
            // Serialize the ChatMessage to Jms.
            Message objMsg = JmsSession.getSession().createObjectMessage(chatMessage);
            publisher.send(objMsg, javax.jms.DeliveryMode.PERSISTENT, javax.jms.Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    
    public void sendAndClose(ChatMessage chatMessage) {

        send(chatMessage);

        // Wait a second before closing.
        new java.util.Timer().schedule( 
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        // your code here
                        close();
                    }
                }, 
                1000 
        );      
    }
    
    public void close() {
        
        try {
            consumer.close();
            publisher.close();
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
