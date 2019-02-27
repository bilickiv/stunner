package hu.uszeged.inf.wlab.stunner.utils.enums;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import android.os.BatteryManager;

/**
 * Enumeration to define the possible plugged states of the battery. Each state
 * has a unique code, according to the {@link BatteryManager} constants starting
 * with BATTERY_PLUGGED_XXX.
 * 
 * @author szelezsant
 * 
 */
public enum BatteryPluggedState {
	/** Battery is plugged to AC power source. */
	AC(BatteryManager.BATTERY_PLUGGED_AC),
	/** Battery is plugged via USB. */
	USB(BatteryManager.BATTERY_PLUGGED_USB),
	/**
	 * Battery is "plugged" to wireless power source. Value comes from constants
	 * for compatibility.
	 */
	WIRELESS(Constants.BATTERY_PLUGGED_WIRELESS),
	/**
	 * Own constant to indicate the state when the battery is not plugged at
	 * all, or the {@link BatteryManager} did not broadcast any information
	 * about the plugged state. In this case this value is used by default.
	 */
	NOT_PLUGGED_OR_UNKNOWN(-1);

	/** The corresponding code defined in {@link BatteryManager} constants. */
	private int code;

	/**
	 * Constructor.
	 * 
	 * @param code
	 *            - the unique code.
	 */
	BatteryPluggedState(final int code) {
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
	 * Gets the {@link BatteryPluggedState} element by the given code.
	 * 
	 * @param code
	 *            - the code to match
	 * @return {@link BatteryPluggedState} element
	 */
	public static BatteryPluggedState getByCode(final int code) {
		BatteryPluggedState returnState;

		switch (code) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			returnState = AC;
			break;

		case BatteryManager.BATTERY_PLUGGED_USB:
			returnState = USB;
			break;

		case Constants.BATTERY_PLUGGED_WIRELESS:
			returnState = WIRELESS;
			break;

		default:
			returnState = NOT_PLUGGED_OR_UNKNOWN;
			break;
		}

		return returnState;
	}

}
