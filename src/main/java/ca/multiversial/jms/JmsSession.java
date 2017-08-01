package ca.multiversial.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import ca.multiversial.model.ChatMessage;

public class JmsSession {
    private static volatile JmsSession instance = null;
    private static Connection connection = null;
    private static Session session = null;
	private static Topic topic = null;

	private JmsSession() {

		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			connection = connectionFactory.createConnection();
			connection.setClientID("multiTopicChat");
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);			
			topic = session.createTopic("defaultTopic?consumer.retroactive=true");

			// Publish
			String payload = "Welcome to the default Topic!";
			/*TextMessage msg = session.createTextMessage(payload);
			MessageProducer publisher = session.createProducer(topic);
			System.out.println("Sending text '" + payload + "'");
			publisher.send(msg, javax.jms.DeliveryMode.PERSISTENT, javax.jms.Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
*/
			ChatMessage message = new ChatMessage();
	        message.setFrom("Admin");
	        message.setContent("Welcome to the default Topic!");
			
			Message objMsg = session.createObjectMessage(message);	
			MessageProducer publisher = session.createProducer(topic);
			System.out.println("Sending text '" + payload + "'");
			publisher.send(objMsg, javax.jms.DeliveryMode.PERSISTENT, javax.jms.Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
			
			connection.start();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
    }

    public static JmsSession getInstance() {
        if (instance == null) {
            synchronized(JmsEndpoint.class) {
                if (instance == null) {
                    instance = new JmsSession();
                }
            }
        }
        return instance;
    }

    private static Connection getConnection() {
		return connection;
	}
    
    public static Session getSession() {
		return session;
	}

	public static Topic getTopic() {
		return topic;
	}
}
