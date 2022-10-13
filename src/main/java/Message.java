import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import io.github.sac.BasicListener;
import io.github.sac.Emitter;
import io.github.sac.ReconnectStrategy;
import io.github.sac.Socket;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class Message extends JSONObject {
    long reqId = (new Date()).getTime();
    public Message(String type, Object obj) {
        this.put("reqId", reqId).put("type", type).put("data", obj);
    }
}
