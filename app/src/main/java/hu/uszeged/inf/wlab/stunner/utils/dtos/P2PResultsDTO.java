package hu.uszeged.inf.wlab.stunner.utils.dtos;

import android.os.Parcel;
import android.os.Parcelable;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.enums.P2PConnectionExitStatus;

public class P2PResultsDTO implements Parcelable {

    public static final Creator<P2PResultsDTO> CREATOR = new Creator<P2PResultsDTO>() {
        @Override
        public P2PResultsDTO createFromParcel(Parcel in) {
            return new P2PResultsDTO(in);
        }

        @Override
        public P2PResultsDTO[] newArray(int size) {
            return new P2PResultsDTO[size];
        }
    };

    private String peerID;
    private String androidID;
    private boolean sender;
    private long connectionStart;
    private long connectionEnd;
    private long channelOpen;
    private long channelClosed;
    private int connectionID;
    private int exitStatus;

    public P2PResultsDTO(){
        peerID = Constants.PREF_STRING_VALUE_EMPTY;
        androidID = Constants.PREF_STRING_VALUE_EMPTY;
        sender = true;
        connectionStart = System.currentTimeMillis();
        connectionID = -1;
        exitStatus = P2PConnectionExitStatus.UNKNOWN.getCode();
    }

    /**
     * Constructor used when regenerating object from parcel.
     *
     * @param parcel - the parcel
     */
    public P2PResultsDTO(final Parcel parcel){
        peerID = parcel.readString();
        androidID = parcel.readString();
        sender = parcel.readInt() == 1;
        connectionStart = parcel.readLong();
        connectionEnd = parcel.readLong();
        channelOpen = parcel.readLong();
        channelClosed = parcel.readLong();
        connectionID = parcel.readInt();
        exitStatus = parcel.readInt();
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(peerID);
        dest.writeString(androidID);
        dest.writeInt(sender ? 1 : 0);
        dest.writeLong(connectionStart);
        dest.writeLong(connectionEnd);
        dest.writeLong(channelOpen);
        dest.writeLong(channelClosed);
        dest.writeInt(connectionID);
        dest.writeInt(exitStatus);
    }

    public String getPeerID() {
        return peerID;
    }

    public void setPeerID(String peerID) {
        this.peerID = peerID;
    }

    public String getAndroidID() {
        return androidID;
    }

    public void setAndroidID(String androidID) {
        this.androidID = androidID;
    }

    public boolean isSender() {
        return sender;
    }

    public void setSender(boolean sender) {
        this.sender = sender;
    }

    public long getConnectionStart() {
        return connectionStart;
    }

    public void setConnectionStart(long connectionStart) {
        this.connectionStart = connectionStart;
    }

    public long getConnectionEnd() {
        return connectionEnd;
    }

    public void setConnectionEnd(long connectionEnd) {
        this.connectionEnd = connectionEnd;
    }

    public long getChannelOpen() {
        return channelOpen;
    }

    public void setChannelOpen(long channelOpen) {
        this.channelOpen = channelOpen;
    }

    public long getChannelClosed() {
        return channelClosed;
    }

    public void setChannelClosed(long channelClosed) {
        this.channelClosed = channelClosed;
    }

    public int getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    @Override
    public String toString() {
        return "P2PResultsDTO{" +
                "peerID='" + peerID + '\'' +
                ", androidID='" + androidID + '\'' +
                ", sender=" + sender +
                ", connectionStart=" + connectionStart +
                ", connectionEnd=" + connectionEnd +
                ", channelOpen=" + channelOpen +
                ", channelClosed=" + channelClosed +
                ", connectionID=" + connectionID +
                ", exitStatus=" + exitStatus +
                '}';
    }
}