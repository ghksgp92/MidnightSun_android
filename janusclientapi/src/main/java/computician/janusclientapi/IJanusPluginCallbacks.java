package computician.janusclientapi; /**
 * Created by ben.trent on 5/7/2015.
 */

import org.json.JSONObject;
import org.webrtc.MediaStream;

/**
 * 서버에 구축된 Janus 플러그인에 대한 콜백 리스너
 * create, join 등을 처리하기 위해 구현해야한다.
 */
public interface IJanusPluginCallbacks extends IJanusCallbacks {
    void success(JanusPluginHandle handle);

    void onMessage(JSONObject msg, JSONObject jsep);

    void onLocalStream(MediaStream stream);

    void onRemoteStream(MediaStream stream);

    void onDataOpen(Object data);

    void onData(Object data);

    void onCleanup();

    void onDetached();

    JanusSupportedPluginPackages getPlugin();
}
