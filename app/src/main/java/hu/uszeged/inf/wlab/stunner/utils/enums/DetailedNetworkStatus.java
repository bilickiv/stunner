package hu.uszeged.inf.wlab.stunner.utils.enums;

import android.net.NetworkInfo;

public enum DetailedNetworkStatus {
    /** Ready to start data connection setup. */
    IDLE(0),
    /** Searching for an available access point. */
    SCANNING(1),
    /** Currently setting up data connection. */
    CONNECTING(2),
    /** Network link established, performing authentication. */
    AUTHENTICATING(3),
    /** Awaiting response from DHCP server in order to assign IP address information. */
    OBTAINING_IPADDR(4),
    /** IP traffic should be available. */
    CONNECTED(5),
    /** IP traffic is suspended */
    SUSPENDED(6),
    /** Currently tearing down data connection. */
    DISCONNECTING(7),
    /** IP traffic not available. */
    DISCONNECTED(8),
    /** Attempt to connect failed. */
    FAILED(9),
    /** Access to this network is blocked. */
    BLOCKED(10),
    /** Link has poor connectivity. */
    VERIFYING_POOR_LINK(11),
    /** Checking if network is a captive portal */
    CAPTIVE_PORTAL_CHECK(12),
    UNKNOWN(-1);

    private int code;

    DetailedNetworkStatus(final int code) { this.code = code; }

    public int getCode() {
        return this.code;
    }

    public static DetailedNetworkStatus getByCode(final int code){
        DetailedNetworkStatus returnState;

        switch (code){
            case 0:
                returnState = IDLE;
                break;
            case 1:
                returnState = SCANNING;
                break;
            case 2:
                returnState = CONNECTING;
                break;
            case 3:
                returnState = AUTHENTICATING;
                break;
            case 4:
                returnState = OBTAINING_IPADDR;
                break;
            case 5:
                returnState = CONNECTED;
                break;
            case 6:
                returnState = SUSPENDED;
                break;
            case 7:
                returnState = DISCONNECTING;
                break;
            case 8:
                returnState = DISCONNECTED;
                break;
            case 9:
                returnState = FAILED;
                break;
            case 10:
                returnState = BLOCKED;
                break;
            case 11:
                returnState = VERIFYING_POOR_LINK;
                break;
            case 12:
                returnState = CAPTIVE_PORTAL_CHECK;
                break;
            default:
                returnState = UNKNOWN;
        }
        return returnState;
    }

    public static DetailedNetworkStatus getByNetworkInfoDetailedState(final NetworkInfo.DetailedState ds){
        DetailedNetworkStatus returnState;
        switch (ds){
            case IDLE:
                returnState = IDLE;
                break;
            case SCANNING:
                returnState = SCANNING;
                break;
            case CONNECTING:
                returnState = CONNECTING;
                break;
            case AUTHENTICATING:
                returnState = AUTHENTICATING;
                break;
            case OBTAINING_IPADDR:
                returnState = OBTAINING_IPADDR;
                break;
            case CONNECTED:
                returnState = CONNECTED;
                break;
            case SUSPENDED:
                returnState = SUSPENDED;
                break;
            case DISCONNECTING:
                returnState = DISCONNECTING;
                break;
            case DISCONNECTED:
                returnState = DISCONNECTED;
                break;
            case FAILED:
                returnState = FAILED;
                break;
            case BLOCKED:
                returnState = BLOCKED;
                break;
            case VERIFYING_POOR_LINK:
                returnState = VERIFYING_POOR_LINK;
                break;
            case CAPTIVE_PORTAL_CHECK:
                returnState = CAPTIVE_PORTAL_CHECK;
                break;
            default:
                returnState = UNKNOWN;
        }
        return returnState;
    }
}
