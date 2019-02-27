package hu.uszeged.inf.wlab.stunner.utils.enums;


import android.content.Intent;
import android.net.ConnectivityManager;

import hu.uszeged.inf.wlab.stunner.utils.Constants;

/**
 * Enumeration to store the possible events which can trigger a discovery test.
 * 
 * @author szelezsant
 * 
 */
public enum DiscoveryTriggerEvents {
	/** User triggered the test from the UI. */
	USER(0, Constants.ACTION_USER),
	/**
	 * Android broadcast with the action
	 * {@link android.net.ConnectivityManager#CONNECTIVITY_ACTION}.
	 */
	CONNECTION_CHANGED(1, ConnectivityManager.CONNECTIVITY_ACTION),
	/**
	 * Android broadcast with the action
	 * {@link android.content.Intent#ACTION_BATTERY_LOW}.
	 */
	BATTERY_LOW(2, Intent.ACTION_BATTERY_LOW),
	/**
	 * Android broadcast with the action
	 * {@link android.content.Intent#ACTION_POWER_CONNECTED}.
	 */
	BATTERY_POWER_CONNECTED(3, Intent.ACTION_POWER_CONNECTED),
	/**
	 * Android broadcast with the action
	 * {@link android.content.Intent#ACTION_POWER_DISCONNECTED}.
	 */
	BATTERY_POWER_DISCONNECTED(4, Intent.ACTION_POWER_DISCONNECTED),
	/**
	 * Scheduled alarm triggered the discovery with the action
	 * {@link hu.uszeged.inf.wlab.stunner.utils.Constants#ACTION_STATE_CHECK}.
	 */
	SCHEDULED_STATE_CHECK(5,Constants.ACTION_STATE_CHECK),
	/** Device boot or the first start of the application. */
	BOOT_COMPLETED(6,Intent.ACTION_BOOT_COMPLETED),
	/**
	 * Special connectivity event, indicating when the connection has been lost.
	 */
	CONNECTION_LOST(7,Constants.ACTION_CONNECTION_LOST),
	/**
	 * Special connectivity event, indicating when the connection has been
	 * established.
	 */
	CONNECTION_ESTABLISHED(8,Constants.ACTION_CONNECTION_ESTABLISHED),
	/** User has turned on/off the background service. */
	SERVICE_TOGGLED(9,Constants.ACTION_SERVICE_TOGGLED),
	ACTION_SHUTDOWN(10,Intent.ACTION_SHUTDOWN),
	/** Another node sends a message while P2P connection is active */
	FIREBASE_MESSAGE_IS_RECEIVED(11,Constants.ACTION_FIREBASE_MESSAGE_IS_RECEIVED),
	SERVICE_TOGGLED_OFF(12,Constants.ACTION_SERVICE_TOGGLED_OFF),
    FIRST_START(13,Constants.ACTION_REGISTER_ALARMS_FIRST_RUN),
    REBOOT(14,Intent.ACTION_REBOOT),
    TIME_CHANGED(15, Intent.ACTION_TIME_CHANGED),
    TIMEZONE_CHANGED(16, Intent.ACTION_TIMEZONE_CHANGED),
    DATE_CHANGED(17, Intent.ACTION_DATE_CHANGED),
	AIRPLANE_MODE_CHANGED(19, Constants.AIRPLANE_MODE_CHANGED),
    /** Unknown event has triggered the discovery. */
	UNKNOWN(-1, "Unknown event has triggered the discovery");

	/** The code to uniquely identify each element. */
	private int code;
	private String name;

	/**
	 * Constructor.
	 * 
	 * @param code
	 *            - the unique code.
	 */
	DiscoveryTriggerEvents(final int code, final String name) {
		this.code = code;
		this.name = name;
	}

	/**
	 * Gets the corresponding code.
	 * 
	 * @return code
	 */
	public int getCode() {
		return this.code;
	}

    public String getName() { return name; }

    /**
	 * Gets the {@link DiscoveryTriggerEvents} element by the given code.
	 * 
	 * @param code
	 *            - the code to match
	 * @return {@link DiscoveryTriggerEvents} element
	 */
	public static DiscoveryTriggerEvents getByCode(final int code) {
		DiscoveryTriggerEvents returnState;

		switch (code) {
		case 0:
			returnState = USER;
			break;

		case 1:
			returnState = CONNECTION_CHANGED;
			break;

		case 2:
			returnState = BATTERY_LOW;
			break;

		case 3:
			returnState = BATTERY_POWER_CONNECTED;
			break;

		case 4:
			returnState = BATTERY_POWER_DISCONNECTED;
			break;

		case 5:
			returnState = SCHEDULED_STATE_CHECK;
			break;

		case 6:
			returnState = BOOT_COMPLETED;
			break;

		case 7:
			returnState = CONNECTION_LOST;
			break;

		case 8:
			returnState = CONNECTION_ESTABLISHED;
			break;

		case 9:
			returnState = SERVICE_TOGGLED;
			break;

		case 10:
			returnState = ACTION_SHUTDOWN;
			break;

        case 11:
            returnState = FIREBASE_MESSAGE_IS_RECEIVED;
            break;

		case 12:
            returnState = SERVICE_TOGGLED_OFF;
            break;

        case 13:
            returnState = FIRST_START;
            break;

        case 14:
            returnState = REBOOT;
            break;

        case 15:
            returnState = TIME_CHANGED;
            break;

        case 16:
            returnState = TIMEZONE_CHANGED;
            break;

		case 17:
            returnState = DATE_CHANGED;
            break;

		case 19:
            returnState = AIRPLANE_MODE_CHANGED;
            break;



		default:
			returnState = UNKNOWN;
			break;
		}

		return returnState;
	}
}
