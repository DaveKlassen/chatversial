package ca.multiversial.websocket;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import ca.multiversial.model.ChatMessage;
import com.google.gson.Gson;

public class MessageDecoder implements Decoder.Text<ChatMessage> {

    private static Gson gson = new Gson();

    @Override
    public ChatMessage decode(String s) throws DecodeException {
        ChatMessage message = gson.fromJson(s, ChatMessage.class);
        return message;
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
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
