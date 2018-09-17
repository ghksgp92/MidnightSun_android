package computician.janusclientapi;

import org.json.JSONObject;

/**
    attach 하는 주체가 뭐야? 나? 아니면 다른 사용자?
 */
interface IJanusAttachPluginCallbacks extends IJanusCallbacks {
    void attachPluginSuccess(JSONObject obj, JanusSupportedPluginPackages plugin, IJanusPluginCallbacks callbacks);
}
