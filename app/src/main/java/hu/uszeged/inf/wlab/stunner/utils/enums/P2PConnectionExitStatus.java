package hu.uszeged.inf.wlab.stunner.utils.enums;

public enum P2PConnectionExitStatus {
    DID_NOT_STARTED(-3),
    CONNECTION_TIMED_OUT(-2),
    FIREBASE_CONNECTION_ERROR(-1),
    NOT_CONNECTED_TO_ANY_NETWORK(0),
    NOBODY_IS_AVAILABLE(1),
    CONNECTION_LOST(2),
    PEER_CONNECTION_LOST(3),
    STUN_SERVER_ERROR(4),
    OFFER_IS_REJECTED(5),
    P2P_CHANNEL_FAILED_TO_OPEN_WITHOUT_SRFLX(10),
    P2P_CHANNEL_FAILED_TO_OPEN_WITH_SRFLX(11),
    P2P_CHANNEL_OPEN_BUT_MESSAGE_ERROR(19),
    P2P_CHANNEL_OPEN_AND_EXCHANGE_MESSAGES_SUCCESSFUL(20),
    UNKNOWN(-10);

    /** The code to uniquely identify each element. */
    private int code;

    /**
     * Constructor.
     *
     * @param code
     *            - the unique code.
     */
    P2PConnectionExitStatus(int code) {
        this.code = code;
    }

    /**
     * Gets the corresponding code.
     *
     * @return code
     */
    public int getCode() {
        return this.code;
    }


    /**
     * @param code
     * @return
     */
    public static P2PConnectionExitStatus getByCode(final int code) {
        P2PConnectionExitStatus returnState;
        switch (code) {
            case -3:
                returnState = DID_NOT_STARTED;
                break;
            case -2:
                returnState = CONNECTION_TIMED_OUT;
                break;
            case -1:
                returnState = FIREBASE_CONNECTION_ERROR;
                break;
            case 0:
                returnState = NOT_CONNECTED_TO_ANY_NETWORK;
                break;
            case 1:
                returnState = NOBODY_IS_AVAILABLE;
                break;
            case 2:
                returnState = CONNECTION_LOST;
                break;
            case 3:
                returnState = PEER_CONNECTION_LOST;
                break;
            case 4:
                returnState = STUN_SERVER_ERROR;
                break;
            case 5:
                returnState = OFFER_IS_REJECTED;
                break;
            case 10:
                returnState = P2P_CHANNEL_FAILED_TO_OPEN_WITHOUT_SRFLX;
                break;
            case 11:
                returnState = P2P_CHANNEL_FAILED_TO_OPEN_WITH_SRFLX;
                break;
            case 19:
                returnState = P2P_CHANNEL_OPEN_BUT_MESSAGE_ERROR;
                break;
            case 20:
                returnState = P2P_CHANNEL_OPEN_AND_EXCHANGE_MESSAGES_SUCCESSFUL;
                break;
            default:
                returnState = UNKNOWN;
        }
        return returnState;
    }

}
