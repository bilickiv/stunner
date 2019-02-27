package hu.uszeged.inf.wlab.stunner.utils.dtos;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * DTO to store uptime information.
 * 
 * @author sarpad
 */
public class UptimeInfoDTO implements  Parcelable {

	/**
	 * Creator used to regenerate original object.
	 */
	public static final Parcelable.Creator<UptimeInfoDTO> CREATOR = new Parcelable.Creator<UptimeInfoDTO>() {

		@Override
		public UptimeInfoDTO createFromParcel(final Parcel source) {
			return new UptimeInfoDTO(source);
		}

		@Override
		public UptimeInfoDTO[] newArray(final int size) {
			return new UptimeInfoDTO[size];
		}
	};

	private long uptime;
	private long shutDownTimestamp;
	private long turnOnTimestamp;

	/**
	 * Default constructor.
	 */
	public UptimeInfoDTO() {
		super();
		uptime = 0;
		shutDownTimestamp = 0;
		turnOnTimestamp = 0;
	}

	/**
	 * Constructor used when recreating object from a parcel.
	 * 
	 * @param parcel - the parcel object to read the data back.
	 */
	public UptimeInfoDTO(final Parcel parcel) {
		uptime = parcel.readLong();
		shutDownTimestamp = parcel.readLong();
		turnOnTimestamp = parcel.readLong();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeLong(uptime);
		dest.writeLong(shutDownTimestamp);
		dest.writeLong(turnOnTimestamp);
	}

	public long getUptime() {
		return uptime;
	}

	public void setUptime(final long uptime) {
		this.uptime = uptime;
	}

	public long getShutDownTimestamp() {
		return shutDownTimestamp;
	}

	public void setShutDownTimestamp(final long shutDownTimestamp) {
		this.shutDownTimestamp = shutDownTimestamp;
	}

	public long getTurnOnTimestamp() {
		return turnOnTimestamp;
	}

	public void setTurnOnTimestamp(final long turnOnTimestamp) {
		this.turnOnTimestamp = turnOnTimestamp;
	}

	@Override
	public String toString() {
		return "UptimeInfoDTO [uptime=" + uptime + ", shutDownTimestamp=" + shutDownTimestamp
				+ ", turnOnTimestamp=" + turnOnTimestamp + "]";
	}

}
