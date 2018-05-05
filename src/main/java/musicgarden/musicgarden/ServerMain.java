package musicgarden.musicgarden;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(WSServer.class);
            }
        };
        
        ContextHandler context = new ContextHandler();
        context.setContextPath("/musicgarden");
        context.setHandler(wsHandler);
        
        
        server.insertHandler(context);
        
        server.start();
        server.join();
    }
}