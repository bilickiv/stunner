package hu.uszeged.inf.wlab.stunner.utils.dtos;

import android.os.Parcel;
import android.os.Parcelable;

import org.codehaus.jackson.annotate.JsonIgnore;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.enums.NatDiscoveryExitStatus;
import hu.uszeged.inf.wlab.stunner.utils.enums.NatDiscoveryResult;

public class NatResultsDTO implements Parcelable {
    public static final Creator<NatResultsDTO> CREATOR = new Creator<NatResultsDTO>() {
        @Override
        public NatResultsDTO createFromParcel(Parcel in) {
            return new NatResultsDTO(in);
        }

        @Override
        public NatResultsDTO[] newArray(int size) {
            return new NatResultsDTO[size];
        }
    };

    private long lastDiscovery;
    /**
     * The corresponding result code of the discovery according to the {@link NatDiscoveryResult} enumeration.
     */
    private int discoveryResult;
    private int exitStatus;
    /** The formatted public IP address. */
    private String publicIP;
    private String STUNserver;

    public NatResultsDTO(){
        publicIP = Constants.PREF_STRING_VALUE_EMPTY;
        STUNserver = Constants.PREF_STRING_VALUE_EMPTY;
        discoveryResult = NatDiscoveryResult.UNKNOWN.getCode();
        exitStatus = NatDiscoveryExitStatus.UNKNOWN.getCode();
    }

    /**
     * Constructor used when recreating object from a parcel.
     *
     * @param parcel - the parcel object to read the data back.
     */
    public NatResultsDTO(final Parcel parcel) {
        discoveryResult = parcel.readInt();
        exitStatus = parcel.readInt();
        publicIP = parcel.readString();
        STUNserver = parcel.readString();
        lastDiscovery = parcel.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(discoveryResult);
        dest.writeInt(exitStatus);
        dest.writeString(publicIP);
        dest.writeString(STUNserver);
        dest.writeLong(lastDiscovery);

    }

    /**
     * Gets the result of the discovery.
     *
     * @return the discoveryResult
     */
    @JsonIgnore
    public NatDiscoveryResult getDiscoveryResult() {
        return NatDiscoveryResult.getByCode(discoveryResult);
    }

    /**
     * Gets the result code of the discovery.
     *
     * @return the discoveryResult
     */
    public int getDiscoveryResultCode() {
        return discoveryResult;
    }

    /**
     * Sets the resultCode of the discovery.
     *
     * @param discoveryResultCode the code of the discoveryResult to set
     */
    public void setDiscoveryResultCode(final int discoveryResultCode) {
        this.discoveryResult = discoveryResultCode;
    }

    public void setDiscoveryResultCode(final NatDiscoveryResult natDiscoveryResult) {
        this.discoveryResult = natDiscoveryResult.getCode();
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(NatDiscoveryExitStatus exitStatus) {
        this.exitStatus = exitStatus.getCode();
    }

    public void setExitStatus(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    /**
     * Gets the public IP address.
     *
     * @return the publicIP
     */
    public String getPublicIP() {
        return publicIP;
    }

    /**
     * Sets the public IP address.
     *
     * @param publicIP the publicIP to set
     */
    public void setPublicIP(final String publicIP) {
        this.publicIP = publicIP;
    }

    public long getLastDiscovery() { return lastDiscovery; }

    public void setLastDiscovery(long lastDiscovery) { this.lastDiscovery = lastDiscovery;  }

    public String getSTUNserver() {
        return STUNserver;
    }

    public void setSTUNserver(String STUNserver) {
        this.STUNserver = STUNserver;
    }

    @Override
    public String toString() {
        return "NatResultsDTO{" +
                "lastDiscovery=" + lastDiscovery +
                ", discoveryResult=" + discoveryResult +
                ", exitStatus=" + exitStatus +
                ", publicIP='" + publicIP + '\'' +
                ", STUNserver='" + STUNserver + '\'' +
                '}';
    }
}
