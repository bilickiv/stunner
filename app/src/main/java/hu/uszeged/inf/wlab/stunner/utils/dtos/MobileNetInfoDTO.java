package hu.uszeged.inf.wlab.stunner.utils.dtos;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;

import android.os.Parcel;
import android.os.Parcelable;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.enums.PhoneType;

/**
 * DTO to store the mobile net information.
 * 
 * @author szelezsant
 */
public class MobileNetInfoDTO implements Parcelable {

	/**
	 * Creator used to regenerate original object.
	 */
	public static final Parcelable.Creator<MobileNetInfoDTO> CREATOR = new Parcelable.Creator<MobileNetInfoDTO>() {

		@Override
		public MobileNetInfoDTO createFromParcel(final Parcel source) {
			return new MobileNetInfoDTO(source);
		}

		@Override
		public MobileNetInfoDTO[] newArray(final int size) {
			return new MobileNetInfoDTO[size];
		}
	};

	/** The name of the carrier. */
	private String carrier;
	/** A constant indicating the device phone type.
	 * This indicates the type of radio used to transmit voice calls. */
	private int phoneType;
	/** The type of the network. */
	private String networkType;
	/** The flag to indicate if the phone is roaming on the current network. */
	private boolean roaming;
	/** The ISO country code equivalent for the SIM provider's country code. */
	private String simCountryIso;
	/** The ISO country code equivalent of the MCC (Mobile Country Code)
	 *  of the current registered operator, or nearby cell information if not registered.
	 * 	Note: Result may be unreliable on CDMA networks. */
	private String networkCountryIso;
	/** Is airplane mode active? */
	private boolean airplane;

	/**
	 * Default constructor.
	 */
	public MobileNetInfoDTO() {
		super();
		carrier = Constants.PREF_STRING_VALUE_EMPTY;
		phoneType = PhoneType.PHONE_TYPE_NONE.getCode();
		networkType = Constants.PREF_STRING_VALUE_EMPTY;
		roaming = false;
		simCountryIso = Constants.PREF_STRING_VALUE_EMPTY;
		networkCountryIso = Constants.PREF_STRING_VALUE_EMPTY;
		airplane = false;
	}

	/**
	 * Constructor used when regenerating object from parcel.
	 * 
	 * @param parcel - the parcel
	 */
	public MobileNetInfoDTO(final Parcel parcel) {
		carrier = parcel.readString();
		phoneType = parcel.readInt();
		networkType = parcel.readString();
		roaming = parcel.readInt() == 1;
		simCountryIso = parcel.readString();
		networkType = parcel.readString();
		airplane = parcel.readInt() == 1;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(carrier);
		dest.writeInt(phoneType);
		dest.writeString(networkType);
		dest.writeInt(roaming ? 1 : 0);
		dest.writeString(simCountryIso);
		dest.writeInt(airplane ? 1 : 0);
	}

	/**
	 * @return the carrier
	 */
	public String getCarrier() {
		return carrier;
	}

	/**
	 * @param carrier the carrier to set
	 */
	public void setCarrier(final String carrier) {
		this.carrier = carrier;
	}

	/**
	 * @return the networkType
	 */
	public String getNetworkType() {
		return networkType;
	}

	/**
	 * @param networkType the networkType to set
	 */
	public void setNetworkType(final String networkType) {
		this.networkType = networkType;
	}

	/**
	 * @return the isRoaming
	 */
	public boolean isRoaming() {
		return roaming;
	}

	/**
	 * @param roaming the isRoaming to set
	 */
	public void setRoaming(final boolean roaming) {
		this.roaming = roaming;
	}


	public String getSimCountryIso() {
		return simCountryIso;
	}

	public void setSimCountryIso(final String simCountryIso) {
		this.simCountryIso = simCountryIso;
	}

	public boolean isAirplane() {
		return airplane;
	}

	public void setAirplane(boolean airplane) {
		this.airplane = airplane;
	}

	public int getPhoneType() {
		return phoneType;
	}

	public void setPhoneType(int code) {
		this.phoneType = code;
	}

	public void setPhoneType(PhoneType pt) {
		this.phoneType = pt.getCode();
	}

	public String getNetworkCountryIso() {
		return networkCountryIso;
	}

	public void setNetworkCountryIso(String networkCountryIso) {
		this.networkCountryIso = networkCountryIso;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		return "MobileNetInfoDTO{" +
				"carrier='" + carrier + '\'' +
				", phoneType=" + phoneType +
				", networkType='" + networkType + '\'' +
				", roaming=" + roaming +
				", simCountryIso='" + simCountryIso + '\'' +
				", networkCountryIso='" + networkCountryIso + '\'' +
				", airplane=" + airplane +
				'}';
	}
}
