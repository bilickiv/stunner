package hu.uszeged.inf.wlab.stunner.utils.enums;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import android.os.BatteryManager;

/**
 * Enumeration to define the possible battery health states according to the
 * BATTERY_HEALTH_XXX constants defined in {@link BatteryManager}.
 * 
 * @author szelezsant
 * 
 */
public enum BatteryHealth {

	/** Battery temperature is under the optimal value. */
	COLD(Constants.BATTERY_HEALT_COLD),
	/** Battery is dead. */
	DEAD(BatteryManager.BATTERY_HEALTH_DEAD),
	/** Battery conditions are good. */
	GOOD(BatteryManager.BATTERY_HEALTH_GOOD),
	/** Battery temperature is over the optimal value. */
	OVERHEAT(BatteryManager.BATTERY_HEALTH_OVERHEAT),
	/** Battery voltage is too high. */
	OVER_VOLTAGE(BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE),
	/**
	 * Battery healt unknown. This is the default value when the
	 * {@link BatteryManager} does not broadcast information about the health.
	 */
	UNKNOWN(BatteryManager.BATTERY_HEALTH_UNKNOWN),
	/**
	 * Unspecified failure occurred when the manager tried to acquire health
	 * data.
	 */
	UNSPECIFIED_FAILURE(BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE);

	/** The corresponding code defined in {@link BatteryManager} constants. */
	private int code;

	/**
	 * Constructor.
	 * 
	 * @param code
	 *            - the unique code.
	 */
	BatteryHealth(final int code) {
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
	 * Gets the corresponding {@link BatteryHealth} element.
	 * 
	 * @param code
	 *            - the code to match
	 * @return {@link BatteryHealth} element
	 */
	public static BatteryHealth getByCode(final int code) {
		BatteryHealth returnHealth;

		switch (code) {
		case Constants.BATTERY_HEALT_COLD:
			returnHealth = COLD;
			break;
		case BatteryManager.BATTERY_HEALTH_DEAD:
			returnHealth = DEAD;
			break;
		case BatteryManager.BATTERY_HEALTH_GOOD:
			returnHealth = GOOD;
			break;
		case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
			returnHealth = BatteryHealth.OVER_VOLTAGE;
			break;
		case BatteryManager.BATTERY_HEALTH_OVERHEAT:
			returnHealth = OVERHEAT;
			break;
		case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
			returnHealth = UNSPECIFIED_FAILURE;
			break;
		default:
			returnHealth = UNKNOWN;
			break;
		}
		return returnHealth;
	}
}
