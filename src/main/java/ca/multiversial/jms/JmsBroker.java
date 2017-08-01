package ca.multiversial.jms;

import java.net.URI;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.FixedCountSubscriptionRecoveryPolicy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
 
@WebListener
public class JmsBroker implements ServletContextListener {
 
    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("The application started");

        // Setup our recovery policy which allows the last 10 messages to be recovered for each new client.
        FixedCountSubscriptionRecoveryPolicy fcsrp = new FixedCountSubscriptionRecoveryPolicy();
        fcsrp.setMaximumSize(10);
        PolicyMap pm = new PolicyMap();
        PolicyEntry pe = new PolicyEntry();
        pe.setSubscriptionRecoveryPolicy(fcsrp);
        pm.setDefaultEntry(pe);

        // Create the JMS Broker.
        BrokerService broker;
        try {
            broker = BrokerFactory.createBroker(new URI("broker:(tcp://localhost:61616)"));
            broker.setDestinationPolicy(pm);
            broker.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Once we have started the broker get the JMS session.
        JmsSession.getInstance();
    }
     
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        System.out.println("The application stopped");
    }
}