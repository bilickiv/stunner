package hu.uszeged.inf.wlab.stunner.utils.enums;

public enum NatDiscoveryExitStatus {
    END_SUCCESSFUL(0),
    ERROR(1),
    SOCKET_EXCEPTION(2),
    UNKNOWN_HOST_EXCEPTION(3),
    MESSAGE_ATTRIBUTE_PARSING_EXCEPTION(4),
    MESSAGE_HEADER_PARSING_EXCEPTION(5),
    UTILITY_EXCEPTION(6),
    IO_EXCEPTION(7),
    MESSAGE_ATTRIBUTE_EXCEPTION(8),
    NULL_POINTER_EXCEPTION(9),
    UNKNOWN(-1);

    /** The code to uniquely identify each element. */
    private int code;

    NatDiscoveryExitStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    /**
     * @param code
     * @return
     */
    public static NatDiscoveryExitStatus getByCode(final int code) {
        NatDiscoveryExitStatus returnState;
        switch (code) {
            case 0:
                returnState = END_SUCCESSFUL;
                break;
            case 1:
                returnState = ERROR;
                break;
            case 2:
                returnState = SOCKET_EXCEPTION;
                break;
            case 3:
                returnState = UNKNOWN_HOST_EXCEPTION;
                break;
            case 4:
                returnState = MESSAGE_ATTRIBUTE_PARSING_EXCEPTION;
                break;
            case 5:
                returnState = MESSAGE_HEADER_PARSING_EXCEPTION;
                break;
            case 6:
                returnState = UTILITY_EXCEPTION;
                break;
            case 7:
                returnState = IO_EXCEPTION;
                break;
            case 8:
                returnState = MESSAGE_ATTRIBUTE_EXCEPTION;
                break;
            case 9:
                returnState = NULL_POINTER_EXCEPTION;
                break;
            default:
                returnState = UNKNOWN;
        }
        return returnState;
    }
}
