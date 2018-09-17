package computician.janusclientapi;

/**
    Janus 와 관련된 모든 콜백 리스너가 상속받는다.
 */
public interface IJanusCallbacks {

    /**
     * 콜백 작업 실패 시, 실패 메세지를 매개변수로 받는다.
     */
    void onCallbackError(String error);
}
