### This is a multi Topic/Room Chat Server created in Java

The chat uses WebSockets to communicate with clients, and internally JMS is used to track the messages sent to different Chat Rooms/Topics. Anytime a user connects to a Topic/Room they receive the last 10 messages sent.

To build the Service you must have Maven installed, and can just type:

- mvn compile jetty:run

in order to start the server. The main webApp is accessed at:

- http://localhost:8080

To create a new chat room the user must type "/Join TopicName"
To see who has typed the most characters visit:

- http://localhost:8080/tally


### I combined these sample codes to initially make the App:

- [A Guide to the Java API for WebSocket](http://www.baeldung.com/java-websockets)
- [This and many other JMS related articles](https://examples.javacodegeeks.com/enterprise-java/jms/jms-topic-example/)