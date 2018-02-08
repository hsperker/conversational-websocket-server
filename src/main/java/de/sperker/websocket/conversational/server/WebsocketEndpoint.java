package de.sperker.websocket.conversational.server;

import de.sperker.websocket.conversational.business.CliBoundary;
import de.sperker.websocket.conversational.business.SocketBoundary;
import de.sperker.websocket.conversational.CustomSpringConfigurator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ServerEndpoint(value = "/ws/", configurator = CustomSpringConfigurator.class)
public class WebsocketEndpoint implements SocketBoundary {

    private Map<String, Session> sessionIndex = new HashMap();

    @Autowired
    private CliBoundary cliBoundary;

    @OnOpen
    public void onOpen(Session session) {
        String clientId = session.getId();
        session.getUserProperties().putIfAbsent("clientId", clientId);
        sessionIndex.put(clientId, session);
        this.cliBoundary.printMessage(String.format("[%s]: connected", clientId));
    }

    @OnMessage
    public void handleMessage(Session session, String inboundMessage) {
        String clientId = (String) session.getUserProperties().get("clientId");
        this.cliBoundary.printMessage(String.format("[%s]: %s", clientId, inboundMessage));
    }

    @OnClose
    public void onClose(Session session) {
        String clientId = (String) session.getUserProperties().get("clientId");
        this.cliBoundary.printMessage(String.format("[%s]: closing connection", clientId));
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String clientId = (String) session.getUserProperties().get("clientId");
        this.cliBoundary.printMessage(String.format("[%s]: error [%s]", clientId, throwable.toString()));
    }

    @Override
    public Set<String> getClients() {
        return this.sessionIndex.keySet();
    }

    @Override
    public void sendMessageToClients(String message) {
        this.sessionIndex.forEach((clientId, session) -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void sendMessageToClient(String clientId, String message) throws IOException {
        this.sessionIndex.get(clientId).getBasicRemote().sendText(message);
    }
}