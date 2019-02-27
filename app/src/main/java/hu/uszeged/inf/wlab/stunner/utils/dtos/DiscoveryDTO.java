package hu.uszeged.inf.wlab.stunner.utils.dtos;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.enums.ConnectionType;
import hu.uszeged.inf.wlab.stunner.utils.enums.DiscoveryTriggerEvents;

import org.codehaus.jackson.annotate.JsonIgnore;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Builder class to construct the data structure which represents a
 * Discovery object.
 * 
 * @author szelezsant
 */
public class DiscoveryDTO implements Parcelable {

	/**
	 * Creator used to regenerate original object.
	 */
	public static final Parcelable.Creator<DiscoveryDTO> CREATOR = new Parcelable.Creator<DiscoveryDTO>() {

		@Override
		public DiscoveryDTO createFromParcel(final Parcel source) {
			return new DiscoveryDTO(source);
		}

		@Override
		public DiscoveryDTO[] newArray(final int size) {
			return new DiscoveryDTO[size];
		}
	};

	/* mandatory fields */
	/** The latitude of the discovery location. */
	private double latitude;
	/** The longitude of the discovery location. */
	private double longitude;
	/** Timestamp of capturing the location info */
	private long locationCaptureTimestamp;
	/** The timestamp of the discovery in milliseconds. */
	private long timeStamp;
	/** The offset of the current timezone relative to the UTC. */
	private int timeZoneUTCOffset;

	/**
	 * The corresponding code of the {@link ConnectionType} enumeration element.
	 */
	private int connectionMode;

	/** The formatted local IP address. */
	private String localIP;
	/** The wifi info dto. */
	private WifiInfoDTO wifiDTO;
	/** The mobile info dto. */
	private MobileNetInfoDTO mobileDTO;
	/** The battery info dto. */
	private BatteryInfoDTO batteryDTO;
	private UptimeInfoDTO uptimeInfoDTO;
	private NatResultsDTO natResultsDTO;
	private WebRTCResultsDTO webRTCResultsDTO;
	/** The code of the triggering event. */
	private int triggerCode;
	/** The current version of the application. */
	private int appVersion;
	private int androidVersion;
    /** The device unique ID */
	private String androidID;
    /** The network state */
	private int networkInfo;
	/** Last known disconnect timestamp from FireBase Server. If disconnected it is 0.  */
	private long lastDisconnect;

	private long recordID;


	/**
	 * Constructor to initialize the default values of some attributes.
	 */
	public DiscoveryDTO() {
		wifiDTO = new WifiInfoDTO();
		mobileDTO = new MobileNetInfoDTO();
		batteryDTO = new BatteryInfoDTO();
		uptimeInfoDTO = new UptimeInfoDTO();
        natResultsDTO = new NatResultsDTO();
		webRTCResultsDTO = new WebRTCResultsDTO();
		localIP = Constants.PREF_STRING_VALUE_EMPTY;
		androidID = Constants.PREF_STRING_VALUE_EMPTY;
		connectionMode = ConnectionType.UNKNOWN.getCode();
		triggerCode = DiscoveryTriggerEvents.UNKNOWN.getCode();
		appVersion = -1;
		networkInfo = -1;
		recordID = 0L;
	}

	/**
	 * Constructor used when recreating object from a parcel.
	 * 
	 * @param parcel - the parcel object to read the data back.
	 */
	public DiscoveryDTO(final Parcel parcel) {
		timeStamp = parcel.readLong();
		latitude = parcel.readDouble();
		longitude = parcel.readDouble();
		locationCaptureTimestamp = parcel.readLong();
		localIP = parcel.readString();
		batteryDTO = parcel.readParcelable(BatteryInfoDTO.class.getClassLoader());
		wifiDTO = parcel.readParcelable(WifiInfoDTO.class.getClassLoader());
		mobileDTO = parcel.readParcelable(MobileNetInfoDTO.class.getClassLoader());
		uptimeInfoDTO = parcel.readParcelable(UptimeInfoDTO.class.getClassLoader());
        natResultsDTO = parcel.readParcelable(NatResultsDTO.class.getClassLoader());
        webRTCResultsDTO = parcel.readParcelable(WebRTCResultsDTO.class.getClassLoader());
		connectionMode = parcel.readInt();
		triggerCode = parcel.readInt();
		appVersion = parcel.readInt();
		androidVersion = parcel.readInt();
		timeZoneUTCOffset = parcel.readInt();
        androidID = parcel.readString();
        networkInfo = parcel.readInt();
		lastDisconnect = parcel.readLong();
		recordID = parcel.readLong();
	}

	/**
	 * Gets the code of the {@link DiscoveryTriggerEvents} element.
	 * 
	 * @return triggerCode.
	 */
	public int getTriggerCode() {
		return triggerCode;
	}

	/**
	 * Sets the {@link DiscoveryTriggerEvents} element which represents the event which started the test.
	 * 
	 * @param trigger - the trigger
	 */
	public void setTriggerCode(final DiscoveryTriggerEvents trigger) {
		triggerCode = trigger.getCode();
	}

	/**
	 * Gets the latitude.
	 * 
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Sets the latitude.
	 * 
	 * @param latitude the latitude to set
	 */
	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Gets the longitude.
	 * 
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Sets the longitude.
	 * 
	 * @param longitude the longitude to set
	 */
	public void setLongitude(final double longitude) {
		this.longitude = longitude;
	}

	public long getLocationCaptureTimestamp() {
		return locationCaptureTimestamp;
	}

	public void setLocationCaptureTimestamp(long locationCaptureTimestamp) {
		this.locationCaptureTimestamp = locationCaptureTimestamp;
	}

	/**
	 * Gets the timestamp.
	 * 
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets the timestamp.
	 * 
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(final long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Gets the time zone.
	 * 
	 * @return the timeZone
	 */
	public int getTimeZone() {
		return timeZoneUTCOffset;
	}

	/**
	 * Sets the time zone.
	 * 
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(final int timeZone) {
		this.timeZoneUTCOffset = timeZone;
	}

	/**
	 * Gets the local IP address.
	 * 
	 * @return the localIP
	 */
	public String getLocalIP() {
		return localIP;
	}

	/**
	 * Sets the local IP address.
	 * 
	 * @param localIP the localIP to set
	 */
	public void setLocalIP(final String localIP) {
		this.localIP = localIP;
	}


	/**
	 * @return the wifiDTO
	 */
	public WifiInfoDTO getWifiDTO() {
		return wifiDTO;
	}

	/**
	 * @param wifiDTO the wifiDTO to set
	 */
	public void setWifiDTO(final WifiInfoDTO wifiDTO) {
		this.wifiDTO = wifiDTO;
	}

	/**
	 * @return the mobileDTO
	 */
	public MobileNetInfoDTO getMobileDTO() {
		return mobileDTO;
	}

	/**
	 * @param mobileDTO the mobileDTO to set
	 */
	public void setMobileDTO(final MobileNetInfoDTO mobileDTO) {
		this.mobileDTO = mobileDTO;
	}

	/**
	 * @return the batteryDTO
	 */
	public BatteryInfoDTO getBatteryDTO() {
		return batteryDTO;
	}

	/**
	 * @param batteryDTO the batteryDTO to set
	 */
	public void setBatteryDTO(final BatteryInfoDTO batteryDTO) {
		this.batteryDTO = batteryDTO;
	}

	/**
	 * Gets the android version code.
	 * 
	 * @return android API level.
	 */
	public int getAndroidVersion() {
		return androidVersion;
	}

	/**
	 * Gets the app version.
	 * 
	 * @return the appVersion
	 */
	public int getAppVersion() {
		return appVersion;
	}

	/**
	 * Sets the app version.
	 * 
	 * @param appVersion - the appVersion to set
	 */
	public void setAppVersion(final int appVersion) {
		this.appVersion = appVersion;
	}

	public void setAndroidVersion(int androidVersion) {
		this.androidVersion = androidVersion;
	}

	/**
	 * Gets the connectionMode.
	 * 
	 * @return the connectionMode
	 */
	public int getConnectionMode() {
		return connectionMode;
	}

	/**
	 * Sets the connection mode.
	 * 
	 * @param connectionMode - the connectionMode to set
	 */
	public void setConnectionMode(final ConnectionType connectionMode) {
		this.connectionMode = connectionMode.getCode();
	}

	public UptimeInfoDTO getUptimeInfoDTO() {
		return uptimeInfoDTO;
	}

	public void setUptimeInfoDTO(final UptimeInfoDTO uptimeInfoDTO) {
		this.uptimeInfoDTO = uptimeInfoDTO;
	}

    public NatResultsDTO getNatResultsDTO() {
        return natResultsDTO;
    }

    public void setNatResultsDTO(NatResultsDTO natResultsDTO) {
        this.natResultsDTO = natResultsDTO;
    }

	public WebRTCResultsDTO getWebRTCResultsDTO() {
		return webRTCResultsDTO;
	}

	public void setWebRTCResultsDTO(WebRTCResultsDTO webRTCResultsDTO) {
		this.webRTCResultsDTO = webRTCResultsDTO;
	}

	public String getAndroidID() {
        return androidID;
    }

    public void setAndroidID(String androidID) {
        this.androidID = androidID;
    }

    public int getNetworkInfo() {
        return networkInfo;
    }

    public void setNetworkInfo(int networkInfo) {
        this.networkInfo = networkInfo;
    }

	public long getLastDisconnect() {
		return lastDisconnect;
	}

	public void setLastDisconnect(long lastDisconnect) {
		this.lastDisconnect = lastDisconnect;
	}

	public long getRecordID() { return recordID; }

	public void setRecordID(long recordID) { this.recordID = recordID; }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		return "DiscoveryDTO{" +
				"latitude=" + latitude +
				", longitude=" + longitude +
				", locationCaptureTimestamp=" + locationCaptureTimestamp +
				", timeStamp=" + timeStamp +
				", timeZoneUTCOffset=" + timeZoneUTCOffset +
				", connectionMode=" + connectionMode +
				", localIP='" + localIP + '\'' +
				", wifiDTO=" + wifiDTO +
				", mobileDTO=" + mobileDTO +
				", batteryDTO=" + batteryDTO +
				", uptimeInfoDTO=" + uptimeInfoDTO +
				", natResultsDTO=" + natResultsDTO +
				", webRTCResultsDTO=" + webRTCResultsDTO +
				", triggerCode=" + triggerCode +
				", appVersion=" + appVersion +
				", androidVersion=" + androidVersion +
				", androidID='" + androidID + '\'' +
				", networkInfo=" + networkInfo +
				", lastDisconnect=" + lastDisconnect +
				", recordID=" + recordID +
				'}';
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeLong(timeStamp);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeLong(locationCaptureTimestamp);
		dest.writeString(localIP);
		dest.writeParcelable(batteryDTO,flags);
		dest.writeParcelable(wifiDTO,flags);
		dest.writeParcelable(mobileDTO,flags);
		dest.writeParcelable(uptimeInfoDTO,flags);
		dest.writeParcelable(natResultsDTO,flags);
		dest.writeParcelable(webRTCResultsDTO,flags);
		dest.writeInt(connectionMode);
		dest.writeInt(triggerCode);
		dest.writeInt(appVersion);
		dest.writeInt(androidVersion);
		dest.writeInt(timeZoneUTCOffset);
		dest.writeString(androidID);
		dest.writeInt(networkInfo);
		dest.writeLong(lastDisconnect);
		dest.writeLong(recordID);
	}
}
