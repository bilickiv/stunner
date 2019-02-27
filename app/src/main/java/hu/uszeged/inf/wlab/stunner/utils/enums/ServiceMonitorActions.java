package hu.uszeged.inf.wlab.stunner.utils.enums;

import hu.uszeged.inf.wlab.stunner.utils.Constants;

public enum ServiceMonitorActions {
    USER_TRIGGERED_SERVICE_START(Constants.USER_TRIGGERED_SERVICE_START),
    BACKGROUND_NAT_DISCOVERY_WEBRTC_TEST_AND_P2P_SERVICE_START(Constants.BACKGROUND_NAT_DISCOVERY_WEBRTC_TEST_AND_P2P_SERVICE_START),
    NAT_DISCOVERY_AND_WEBRTC_TEST_SERVICE_FINISHED(Constants.NAT_DISCOVERY_AND_WEBRTC_TEST_SERVICE_FINISHED),
    P2P_SERVICE_FINISHED(Constants.P2P_SERVICE_FINISHED),
    FIREBASE_MESSAGE_IS_RECEIVED(Constants.FIREBASE_MESSAGE_IS_RECEIVED),
    STOP_EVERYTHING(Constants.STOP_EVERYTHING),
    FIREBASE_UPDATE_STATE(Constants.FIREBASE_UPDATE_STATE),
    SERVICE_MONITOR_RESTART_IS_REQUIRED(Constants.SERVICE_MONITOR_RESTART_IS_REQUIRED),
    SERVICE_START_IS_NOT_NECESSARY(Constants.SERVICE_START_IS_NOT_NECESSARY);

    String serviceStarterString;
    ServiceMonitorActions(String serviceStarterString) {
        this.serviceStarterString = serviceStarterString;
    }

    public String getServiceStarterString() {
        return serviceStarterString;
    }

    public static ServiceMonitorActions getByServiceStarterString(String serviceStarterString){
        switch (serviceStarterString){
            case Constants.USER_TRIGGERED_SERVICE_START :
                return USER_TRIGGERED_SERVICE_START;
            case Constants.BACKGROUND_NAT_DISCOVERY_WEBRTC_TEST_AND_P2P_SERVICE_START:
                return BACKGROUND_NAT_DISCOVERY_WEBRTC_TEST_AND_P2P_SERVICE_START;
            case Constants.NAT_DISCOVERY_AND_WEBRTC_TEST_SERVICE_FINISHED:
                return NAT_DISCOVERY_AND_WEBRTC_TEST_SERVICE_FINISHED;
            case Constants.P2P_SERVICE_FINISHED :
                return P2P_SERVICE_FINISHED;
            case Constants.STOP_EVERYTHING:
                return STOP_EVERYTHING;
            case Constants.FIREBASE_MESSAGE_IS_RECEIVED:
                return FIREBASE_MESSAGE_IS_RECEIVED;
            case Constants.FIREBASE_UPDATE_STATE:
                return FIREBASE_UPDATE_STATE;
            case Constants.SERVICE_MONITOR_RESTART_IS_REQUIRED:
                return SERVICE_MONITOR_RESTART_IS_REQUIRED;
            default:
                return SERVICE_START_IS_NOT_NECESSARY;
        }
    }
}
