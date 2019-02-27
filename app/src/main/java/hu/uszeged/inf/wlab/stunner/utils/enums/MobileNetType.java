package hu.uszeged.inf.wlab.stunner.utils.enums;

import android.telephony.TelephonyManager;

/** Enumeration to store individual mobile network types and related data. */
public enum MobileNetType {
	/** Unknown. */
	UNKNOWN(TelephonyManager.NETWORK_TYPE_UNKNOWN, -1.0f, "UNKNOWN"),
	/** GPRS. */
	GPRS(TelephonyManager.NETWORK_TYPE_GPRS, 0.11f, "GPRS"),
	/** EDGE. */
	EDGE(TelephonyManager.NETWORK_TYPE_EDGE, 0.28f, "EDGE"),
	/** UMTS. */
	UMTS(TelephonyManager.NETWORK_TYPE_UMTS, 14.4f, "UMTS"),
	/** CDMA. */
	CDMA(TelephonyManager.NETWORK_TYPE_CDMA, 0.01f, "CDMA"),
	/** EV-DO rel.0. */
	EVDO_0(TelephonyManager.NETWORK_TYPE_EVDO_0, 2.45f, "EV-DO_Rel.0"),
	/** EV-DO rev.A. */
	EVDO_A(TelephonyManager.NETWORK_TYPE_EVDO_A, 3.1f, "EV-DO_Rev.A"),
	/** 1xRTT. */
	XRTT(TelephonyManager.NETWORK_TYPE_1xRTT, 0.3f, "1xRTT"),
	/** HSDPA. */
	HSDPA(TelephonyManager.NETWORK_TYPE_HSDPA, 14.4f, "HSDPA"),
	/** HSUPA. */
	HSUPA(TelephonyManager.NETWORK_TYPE_HSUPA, 34.5f, "HSUPA"),
	/** HSPA. */
	HSPA(TelephonyManager.NETWORK_TYPE_HSPA, 14.4f, "HSPA"),
	/** iDEN. */
	IDEN(TelephonyManager.NETWORK_TYPE_IDEN, 0.1f, "iDEN"),
	/** EV-DO rev.B. */
	EVDO_B(TelephonyManager.NETWORK_TYPE_EVDO_B, 14.7f, "EV-DO_Rev.B"),
	/** LTE. */
	LTE(TelephonyManager.NETWORK_TYPE_LTE, 100f, "LTE"),
	/** eHRPD. */
	EHRPD(TelephonyManager.NETWORK_TYPE_EHRPD, 2.4f, "eHRPD"),
	/** HSPAP. */
	HSPAP(TelephonyManager.NETWORK_TYPE_HSPAP, 42f, "HSPA+"),
	/** GSM */
	GSM(TelephonyManager.NETWORK_TYPE_GSM, 0.01f, "GSM"),
	/** TD SCDMA */
	TD_SCDMA(TelephonyManager.NETWORK_TYPE_TD_SCDMA, 14.4f, "TD_SCDMA"),
	/** IWLAN */
	IWLAN(TelephonyManager.NETWORK_TYPE_IWLAN, 1733.0f,"IWLAN");

	/**
	 * Unique code according to the {@link android.telephony.TelephonyManager
	 * TelephonyManager}.
	 */
	private int code;
	/** Estimated download bandwidth in MBps. */
	private float bandwith;
	/** Human-readable name. */
	private String name;

	/**
	 * Constructor.
	 * 
	 * @param code
	 *            - unique code to identify an element.
	 * @param bandwidth
	 *            - the maximal DOWNLOAD bandwidth in MBps.
	 * @param name
	 *            - the human-readable name of the network type.
	 */
	MobileNetType(final int code, final float bandwidth, final String name) {
		this.code = code;
		this.bandwith = bandwidth;
		this.name = name;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @return the bandwith
	 */
	public float getBandwith() {
		return bandwith;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the element by code.
	 * 
	 * @param code
	 *            - the corresponding code
	 * @return {@link MobileNetType} element
	 */
	public static MobileNetType getByCode(final int code) {
		MobileNetType returnValue = UNKNOWN;
		switch (code) {
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			returnValue = UNKNOWN;
			break;
		case TelephonyManager.NETWORK_TYPE_GPRS:
			returnValue = GPRS;
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE:
			returnValue = EDGE;
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			returnValue = UMTS;
			break;
		case TelephonyManager.NETWORK_TYPE_CDMA:
			returnValue = CDMA;
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			returnValue = EVDO_0;
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			returnValue = EVDO_A;
			break;
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			returnValue = XRTT;
			break;
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			returnValue = HSDPA;
			break;
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			returnValue = HSUPA;
			break;
		case TelephonyManager.NETWORK_TYPE_HSPA:
			returnValue = HSPA;
			break;
		case TelephonyManager.NETWORK_TYPE_IDEN:
			returnValue = IDEN;
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			returnValue = EVDO_B;
			break;
		case TelephonyManager.NETWORK_TYPE_LTE:
			returnValue = LTE;
			break;
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			returnValue = EHRPD;
			break;
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			returnValue = HSPAP;
			break;
		case TelephonyManager.NETWORK_TYPE_GSM:
			returnValue = GSM;
			break;
		case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
			returnValue = TD_SCDMA;
			break;
		case TelephonyManager.NETWORK_TYPE_IWLAN:
			returnValue = IWLAN;
			break;
		default:
			break;
		}
		return returnValue;
	}

}
