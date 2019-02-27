package hu.uszeged.inf.wlab.stunner.utils.enums;

import android.telephony.TelephonyManager;

public enum PhoneType {
    PHONE_TYPE_NONE(0),
    PHONE_TYPE_GSM(1),
    PHONE_TYPE_CDMA(2),
    PHONE_TYPE_SIP(3);

    private int code;

    /**
     * Constructor.
     *
     * @param code
     *            - the unique code.
     */
    PhoneType(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PhoneType getByCode(int code){
        switch (code){
            case TelephonyManager.PHONE_TYPE_NONE:
                return PHONE_TYPE_NONE;
            case TelephonyManager.PHONE_TYPE_GSM:
                return PHONE_TYPE_GSM;
            case TelephonyManager.PHONE_TYPE_CDMA:
                return PHONE_TYPE_CDMA;
            case TelephonyManager.PHONE_TYPE_SIP:
                return PHONE_TYPE_SIP;
            default:
                return PHONE_TYPE_NONE;
        }
    }
}
