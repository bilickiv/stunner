package hu.uszeged.inf.wlab.stunner.utils.enums;

import android.net.wifi.WifiManager;

public enum WifiState {
    WIFI_STATE_DISABLING(WifiManager.WIFI_STATE_DISABLING),
    WIFI_STATE_DISABLED(WifiManager.WIFI_STATE_DISABLED),
    WIFI_STATE_ENABLING(WifiManager.WIFI_STATE_ENABLING),
    WIFI_STATE_ENABLED(WifiManager.WIFI_STATE_ENABLED),
    WIFI_STATE_UNKNOWN(WifiManager.WIFI_STATE_UNKNOWN);
    private int code;
    WifiState(int code) {
        this.code = code;
    }
    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    public static WifiState getByCode(final int code) {
        switch (code) {
            case(WifiManager.WIFI_STATE_DISABLING):
                return WIFI_STATE_DISABLED;
            case(WifiManager.WIFI_STATE_DISABLED):
                return WIFI_STATE_DISABLED;
            case(WifiManager.WIFI_STATE_ENABLING):
                return WIFI_STATE_ENABLING;
            case(WifiManager.WIFI_STATE_ENABLED):
                return WIFI_STATE_ENABLED;
            case(WifiManager.WIFI_STATE_UNKNOWN):
                return WIFI_STATE_UNKNOWN;
            default:
                return WIFI_STATE_UNKNOWN;
        }
    }
}
