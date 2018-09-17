package computician.janusclientapi;

import org.webrtc.PeerConnection;

import java.util.List;

/**
    Janus 게이트웨이와 연결하는데 사용하는 콜백 리스너
 */
public interface IJanusGatewayCallbacks extends IJanusCallbacks {

    void onSuccess();

    void onDestroy();

    String getServerUri();

    List<PeerConnection.IceServer> getIceServers();

    Boolean getIpv6Support();

    Integer getMaxPollEvents();
}
