package hu.uszeged.inf.wlab.stunner.utils.enums;

import hu.uszeged.inf.wlab.stunner.utils.Constants;

public enum P2PServiceState {
    IDLE(Constants.IDLE),
    HAVE_ALREADY_STARTED(Constants.HAVE_ALREADY_STARTED),
    HAVE_ALREADY_FINISHED_BUT_WAITING_FOR_OTHER_SERVICES_RESULTS(Constants.HAVE_ALREADY_FINISHED_BUT_WAITING_FOR_OTHER_SERVICES_RESULTS);


    String serviceStateString;
    P2PServiceState(String serviceStateString) {
        this.serviceStateString = serviceStateString;
    }

    public String getServiceStateString() {
        return serviceStateString;
    }

    public static P2PServiceState getByServiceStarterString(String serviceStateString){
        switch (serviceStateString){
            case Constants.IDLE :
                return IDLE;
            case Constants.HAVE_ALREADY_STARTED:
                return HAVE_ALREADY_STARTED;
            case Constants.HAVE_ALREADY_FINISHED_BUT_WAITING_FOR_OTHER_SERVICES_RESULTS:
                return HAVE_ALREADY_FINISHED_BUT_WAITING_FOR_OTHER_SERVICES_RESULTS;
            default:
                return IDLE;
        }
    }
}
