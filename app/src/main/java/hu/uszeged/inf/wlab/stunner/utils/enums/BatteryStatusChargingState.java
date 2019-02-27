package hu.uszeged.inf.wlab.stunner.utils.enums;

import android.os.BatteryManager;

/**
 * Enumeration to define the possible charging state according to the constants
 * defined in {@link BatteryManager}.
 * 
 * @author szelezsant
 * 
 */
public enum BatteryStatusChargingState {
	/** Charging state unknown. */
	UNKNOWN(BatteryManager.BATTERY_STATUS_UNKNOWN),
	/** The device is charging from a specified source. */
	CHARGING(BatteryManager.BATTERY_STATUS_CHARGING),
	/**
	 * The device is not connected to any charger, power is supplied by its own
	 * battery.
	 */
	DISCHARGING(BatteryManager.BATTERY_STATUS_DISCHARGING),
	/**
	 * The device is not charging for some reason. Android documentation does
	 * not mention possible explanations.
	 */
	NOT_CHARGING(BatteryManager.BATTERY_STATUS_NOT_CHARGING),
	/** Battery is full. */
	FULL(BatteryManager.BATTERY_STATUS_FULL);

	/**
	 * The unique code to define the charging state, according to the
	 * {@link BatteryManager} constant.
	 */
	private int code;

	/**
	 * Constructs the element.
	 * 
	 * @param code
	 *            - the unique code
	 */
	BatteryStatusChargingState(final int code) {
		this.code = code;
	}

	/**
	 * Gets the unique code of the state.
	 * 
	 * @return code
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * Gets the {@link BatteryStatusChargingState} element which corresponds to
	 * the given code.
	 * 
	 * @param code
	 *            - the code to match the element
	 * @return {@link BatteryStatusChargingState} element
	 */
	public static BatteryStatusChargingState getByCode(final int code) {
		BatteryStatusChargingState returnState;

		switch (code) {
		case BatteryManager.BATTERY_STATUS_CHARGING:
			returnState = CHARGING;
			break;
		case BatteryManager.BATTERY_STATUS_DISCHARGING:
			returnState = DISCHARGING;
			break;
		case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
			returnState = NOT_CHARGING;
			break;
		case BatteryManager.BATTERY_STATUS_FULL:
			returnState = FULL;
			break;
		default:
			returnState = UNKNOWN;
			break;
		}

		return returnState;
	}

}
