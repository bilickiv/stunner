package hu.uszeged.inf.wlab.stunner.utils.dtos;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.enums.BatteryHealth;
import hu.uszeged.inf.wlab.stunner.utils.enums.BatteryPluggedState;
import hu.uszeged.inf.wlab.stunner.utils.enums.BatteryStatusChargingState;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * DTO to store battery information.
 * 
 * @author szelezsant
 */
public class BatteryInfoDTO implements Parcelable {

	/**
	 * Creator used to regenerate original object.
	 */
	public static final Parcelable.Creator<BatteryInfoDTO> CREATOR = new Parcelable.Creator<BatteryInfoDTO>() {

		@Override
		public BatteryInfoDTO createFromParcel(final Parcel source) {
			return new BatteryInfoDTO(source);
		}

		@Override
		public BatteryInfoDTO[] newArray(final int size) {
			return new BatteryInfoDTO[size];
		}
	};

	/** The remaining percentage. */
	private int percentage;
	/**
	 * The charging state according to the values stored in {@link android.os.BatteryManager BatteryManager}.
	 */
	private int chargingState;
	/**
	 * The health according to the values stored in {@link android.os.BatteryManager BatteryManager}.
	 */
	private int health;
	/** The temperature value. */
	private int temperature;
	/** The voltage. */
	private int voltage;
	/** The plugged state value. */
	private int pluggedState;
	/** Indicates if the battery is present. Null if unspecified. */
	private Boolean present;
	/** The human readable name of the battery technology. Null if unspecified. */
	private String technology;

	/**
	 * Constructor.
	 */
	public BatteryInfoDTO() {
		super();
		technology = Constants.PREF_STRING_VALUE_EMPTY;
		percentage = -1;
		chargingState = BatteryStatusChargingState.UNKNOWN.getCode();
		pluggedState = BatteryPluggedState.NOT_PLUGGED_OR_UNKNOWN.getCode();
	}

	/**
	 * Constructor used to regenerate the object from a parcel.
	 * 
	 * @param parcel - the parcel
	 */
	public BatteryInfoDTO(final Parcel parcel) {
		percentage = parcel.readInt();
		chargingState = parcel.readInt();
		health = parcel.readInt();
		temperature = parcel.readInt();
		voltage = parcel.readInt();
		pluggedState = parcel.readInt();
		technology = parcel.readString();
		present = 1 == parcel.readInt();
	}

	/**
	 * @return the percentage
	 */
	public int getPercentage() {
		return percentage;
	}

	/**
	 * @param percentage the percentage to set
	 */
	public void setPercentage(final int percentage) {
		this.percentage = percentage;
	}

	/**
	 * @return the chargingState
	 */
	public int getChargingState() {
		return chargingState;
	}

	/**
	 * @param state the chargingState to set
	 */
	public void setChargingState(final BatteryStatusChargingState state) {
		this.chargingState = state.getCode();
	}

	/**
	 * @return the health
	 */
	public int getHealth() {
		return health;
	}

	/**
	 * @param health the health to set
	 */
	public void setHealth(final BatteryHealth health) {
		this.health = health.getCode();
	}

	/**
	 * @return the temperature
	 */
	public int getTemperature() {
		return temperature;
	}

	/**
	 * @param temperature the temperature to set
	 */
	public void setTemperature(final int temperature) {
		this.temperature = temperature;
	}

	/**
	 * @return the voltage
	 */
	public int getVoltage() {
		return voltage;
	}

	/**
	 * @param voltage the voltage to set
	 */
	public void setVoltage(final int voltage) {
		this.voltage = voltage;
	}

	/**
	 * @return the pluggedState
	 */
	public int getPluggedState() {
		return pluggedState;
	}

	/**
	 * @param state the pluggedState to set
	 */
	public void setPluggedState(final BatteryPluggedState state) {
		this.pluggedState = state.getCode();
	}

	/**
	 * @return the present
	 */
	public Boolean getPresent() {
		return present;
	}

	/**
	 * @param present the present to set
	 */
	public void setPresent(final Boolean present) {
		this.present = present;
	}

	/**
	 * @return the technology
	 */
	public String getTechnology() {
		return technology;
	}

	/**
	 * @param technology the technology to set
	 */
	public void setTechnology(final String technology) {
		this.technology = technology;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(percentage);
		dest.writeInt(chargingState);
		dest.writeInt(health);
		dest.writeInt(temperature);
		dest.writeInt(voltage);
		dest.writeInt(pluggedState);
		dest.writeString(technology);
		dest.writeInt(null != present && present ? 1 : 0);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BatteryInfoDTO [percentage=" + percentage + ", chargingState=" + chargingState + ", health="
				+ health
				+ ", temperature=" + temperature + ", voltage=" + voltage + ", pluggedState=" + pluggedState + ", present=" + present
				+ ", technology="
				+ technology + "]";
	}
}
