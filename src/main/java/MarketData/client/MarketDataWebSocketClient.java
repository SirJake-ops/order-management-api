package MarketData.client;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Component
public class MarketDataWebSocketClient implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(MarketDataWebSocketClient.class);

    private final HttpClient httpClient;
    private final MarketDataWebSocketMessageParser messageParser;
    private final MarketDataStreamState streamState;
    private final boolean websocketEnabled;
    private final URI websocketUri;

    private volatile WebSocket webSocket;

    public MarketDataWebSocketClient(
            MarketDataWebSocketMessageParser messageParser,
            MarketDataStreamState streamState,
            @Value("${market-data.websocket.enabled:true}") boolean websocketEnabled,
            @Value("${market-data.websocket.url:ws://localhost:8080/ws}") String websocketUrl
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.messageParser = messageParser;
        this.streamState = streamState;
        this.websocketEnabled = websocketEnabled;
        this.websocketUri = URI.create(websocketUrl);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!websocketEnabled) {
            logger.info("Market data websocket client is disabled");
            return;
        }

        try {
            webSocket = httpClient.newWebSocketBuilder()
                    .buildAsync(websocketUri, new Listener())
                    .join();
        } catch (RuntimeException exception) {
            streamState.markDisconnected();
            logger.warn("Unable to connect to market data websocket at {}", websocketUri, exception);
        }
    }

    @PreDestroy
    public void shutdown() {
        Optional.ofNullable(webSocket).ifPresent(socket -> {
            try {
                socket.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown").join();
            } catch (RuntimeException exception) {
                logger.debug("Market data websocket close failed", exception);
            }
        });
    }

    private final class Listener implements WebSocket.Listener {
        private final StringBuilder partialMessage = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            streamState.markConnected();
            logger.info("Connected to market data websocket at {}", websocketUri);
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            partialMessage.append(data);
            if (last) {
                final String message = partialMessage.toString();
                partialMessage.setLength(0);
                messageParser.parse(message).ifPresent(streamState::updateLatestPrice);
            }

            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            streamState.markDisconnected();
            logger.info("Market data websocket closed with status {} and reason '{}'", statusCode, reason);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            streamState.markDisconnected();
            logger.warn("Market data websocket error", error);
        }
    }
}
