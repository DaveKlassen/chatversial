package ca.multiversial.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import ca.multiversial.model.ChatMessage;
import com.google.gson.Gson;

public class MessageEncoder implements Encoder.Text<ChatMessage> {

    private static Gson gson = new Gson();

    @Override
    public String encode(ChatMessage message) throws EncodeException {
        String json = gson.toJson(message);
        return json;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }

    @Override
    public void destroy() {
        // Close resources
    }
}
