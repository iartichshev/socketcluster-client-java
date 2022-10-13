import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import io.github.sac.*;


import java.util.Calendar;
import java.util.List;
import java.util.Map;


/**
 * Created by iartichshev@crystalspring.kz on 12/10/2022.
 */

public class Main {

    private static final String url= "https://test.cs.kz:1234/sc_mobile/";
    private static final String secret = "123456";
    private static final String login = "2BE175BB64";

    private static String getJWT(String login) {
        Calendar vCalendar = Calendar.getInstance();
        vCalendar.add(Calendar.MINUTE, 5); // current date  + 5 minutes
        return JWT.create()
                .withIssuer("CS")
                .withAudience("SBI")
                .withExpiresAt(vCalendar.getTime())
                .withClaim("u", login)
                .sign(Algorithm.HMAC256(secret));
    }

    public static void main(String arg[]) {
        Socket socket = new Socket(url);
        socket.setListener(new BasicListener() {

            public void onConnected(Socket socket,Map<String, List<String>> headers) {
                System.out.println("Connected to endpoint");
            }

            public void onDisconnected(Socket socket,WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
                System.out.println("Disconnected from end-point");
            }

            public void onConnectError(Socket socket,WebSocketException exception) {
                System.out.println("Got connect error "+ exception);
            }

            public void onSetAuthToken(String token, Socket socket) {
                System.out.println("Set auth token got called");
                socket.setAuthToken(token);
            }

            public void onAuthentication(Socket socket,Boolean status) {
                if (status) {
                    System.out.println("socket is authenticated");
                } else {
                    System.out.println("Authentication is required");
                }
            }
        });
        socket.setAuthToken(getJWT(login));
        socket.setReconnection(new ReconnectStrategy().setDelay(3000).setMaxAttempts(10));
        socket.disableLogging();
        try {
            socket.connect();

            // subs to users channel
            Socket.Channel channel = socket.createChannel("sbi." + login);
            channel.onMessage(new Emitter.Listener() {
                public void call(String channel, Object data) {
                    // ignore any with data.code=1!
                    System.out.println(channel + ": got message " + data);
                }
            });

            Thread.sleep(1000); // wait for connect?
//            while (!socket.isconnected()) { // don't work!?
//                Thread.sleep(100);
//            }


            socket.emit("sbi.sc", new Message("ping", "test"));
//            socket.emit("sbi.sc", new Message("kase.getSec", new JSONObject().put("secCode","KZAP")));
//            socket.emit("sbi.sc", new Message("rep.iex.news.v2", new JSONArray().put("AAPL").put("FB")));
//            socket.emit("sbi.sc", new Message("rep.iex.quote.v2", new JSONObject().put("stock", new JSONArray().put("AAPL").put("FB"))));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
