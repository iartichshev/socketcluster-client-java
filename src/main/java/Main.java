import com.auth0.jwt.JWT;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import io.github.sac.*;
import org.json.JSONObject;

import java.util.*;

/**
 * iartichshev@crystalspring.kz on 13/10/2022.
 */

public class Main {
    private static final String url= "https://test.cs.kz:1234/sc_mobile/";
    private static final String login = "login";
    private static final String pass = "pass";
    private static String user;


    private static Socket socketCreate() throws Exception {
        Socket vSocket = new Socket(url);
        vSocket.setListener(new BasicListener() {
            public void onConnected(Socket socket, Map<String, List<String>> headers) {
                System.out.println("Connected");
            }
            public void onDisconnected(Socket socket,WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
                System.out.println("Disconnected");
            }
            public void onConnectError(Socket socket,WebSocketException exception) {
                System.out.println("Сonnection error "+ exception);
            }
            public void onSetAuthToken(String token, Socket socket) {
                socket.setAuthToken(token);
                // берем из предоставленного токена значение user, чтобы подписаться на личный канал
                user = JWT.decode(token).getClaim("u").asString();
                System.out.println("Received auth token, user=" + user);
                // и здесь же подписываемся на приватные сообщения
                subscribeToPrivateChannel(socket, user);
            }
            public void onAuthentication(Socket socket, Boolean status) {
                if (status) {
                    System.out.println("Authenticated");
                } else {
                    System.out.println("Authentication is required");
                }
            }
        });
        vSocket.setReconnection(new ReconnectStrategy().setDelay(3000).setMaxAttempts(10));
        vSocket.disableLogging();
        // sbi.connect высылается клиенту до подписки, можно здесь получить значенеи user после аутентификации без парсинга jwt
        vSocket.on("sbi.connect", new Emitter.Listener() {
            public void call(String eventName, Object object) {
                System.out.println("on: " + eventName + " data is " + object);
            }
        });
        vSocket.connect();
        // отправим логин/пароль для аутентификации
        vSocket.emit("sbi.sc",
                new Message(
                        "auth.login",
                        new JSONObject()
                                .put("login",login)
                                .put("pass", EncryptHelper.encrypt(pass))
                )
        );
        // здесь можем подождать когда пришлют токен после проверки пароля
        // такое ожидание - только для теста!
        while (user == null) {
            Thread.sleep(100);
        }
        return vSocket;
    }



    private static void subscribeToPrivateChannel(Socket socket, String user) {
        String vChannel = "sbi." + user;
        System.out.println("Subscribe to channel " + vChannel);
        Socket.Channel channel = socket.createChannel(vChannel);
        channel.subscribe();
        channel.onMessage(new Emitter.Listener() {
            public void call(String channel, Object data) {
                // ignore any with data.code=1!
                System.out.println(channel + ": " + data);
            }
        });
    }
    private static void socketSend(Socket socket, Message message) {
        socket.emit("sbi.sc", message);
    }

    public static void main(String arg[]) {
        try {
            Socket socket = socketCreate();

//            subscribeToPrivateChannel(socket, user);

            // проверим отправку запросов и прием результатов
            for (int i = 0; i<10; i++) {
                Thread.sleep(1000);
                socketSend(socket, new Message("ping", "test" + i));
//                socketSend(socket, new Message("kase.getSec", new JSONObject().put("secCode","KZAP")));
//                socketSend(socket,  new Message("rep.iex.news.v2", new JSONArray().put("AAPL").put("FB")));
//                socketSend(socket,  new Message("rep.iex.quote.v2", new JSONObject().put("stock", new JSONArray().put("AAPL").put("FB"))));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
