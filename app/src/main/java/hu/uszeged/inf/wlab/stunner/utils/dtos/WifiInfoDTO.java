package hu.uszeged.inf.wlab.stunner.utils.dtos;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;

import android.os.Parcel;
import android.os.Parcelable;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.enums.WifiState;

/**
 * DTO to store wifi information.
 * 
 * @author szelezsant
 */
public class WifiInfoDTO implements  Parcelable {

	/**
	 * Creator used to regenerate original object.
	 */
	public static final Parcelable.Creator<WifiInfoDTO> CREATOR = new Parcelable.Creator<WifiInfoDTO>() {

		@Override
		public WifiInfoDTO createFromParcel(final Parcel source) {
			return new WifiInfoDTO(source);
		}

		@Override
		public WifiInfoDTO[] newArray(final int size) {
			return new WifiInfoDTO[size];
		}
	};


	/** The unqiue ssid. */
	private String ssid;
	/** The bandwidth. */
	private String bandwidth;
	/** The mac address of the network interface. */
	private String macAddress;
	/** The received signal strenght index. */
	private int rssi;
	/** The state of wifi */
	private int state;


	/**
	 * Default constructor.
	 */
	public WifiInfoDTO() {
		super();
		ssid = Constants.PREF_STRING_VALUE_EMPTY;
		bandwidth = Constants.PREF_STRING_VALUE_EMPTY;
		macAddress = Constants.PREF_STRING_VALUE_EMPTY;
		rssi = 0;
		state = 1;
	}

	/**
	 * Constructor used when recreating object from a parcel.
	 * 
	 * @param parcel
	 *            - the parcel object to read the data back.
	 */
	public WifiInfoDTO(final Parcel parcel) {
		ssid = parcel.readString();
		bandwidth = parcel.readString();
		macAddress = parcel.readString();
		rssi = parcel.readInt();
		state = parcel.readInt();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(ssid);
		dest.writeString(bandwidth);
		dest.writeString(macAddress);
		dest.writeInt(rssi);
		dest.writeInt(state);
	}

	/**
	 * @return the ssid
	 */
	public String getSsid() {
		return ssid;
	}

	/**
	 * @param ssid
	 *            the ssid to set
	 */
	public void setSsid(final String ssid) {
		this.ssid = ssid;
	}

	/**
	 * @return the bandwidth
	 */
	public String getBandwidth() {
		return bandwidth;
	}

	/**
	 * @param bandwidth
	 *            the bandwidth to set
	 */
	public void setBandwidth(final String bandwidth) {
		this.bandwidth = bandwidth;
	}

	/**
	 * @return the macAddress
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * @param macAddress
	 *            the macAddress to set
	 */
	public void setMacAddress(final String macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * @return the rssi
	 */
	public int getRssi() {
		return rssi;
	}

	/**
	 * @param rssi
	 *            the rssi to set
	 */
	public void setRssi(final int rssi) {
		this.rssi = rssi;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setState(WifiState state) {
		this.state = state.getCode();
	}

	@Override
	public String toString() {
		return "WifiInfoDTO{" +
				", ssid='" + ssid + '\'' +
				", bandwidth='" + bandwidth + '\'' +
				", macAddress='" + macAddress + '\'' +
				", rssi=" + rssi +
				", state=" + state +
				'}';
	}
}
