package hu.uszeged.inf.wlab.stunner.utils.dtos;

import android.os.Parcel;
import android.os.Parcelable;

import hu.uszeged.inf.wlab.stunner.utils.enums.P2PConnectionExitStatus;

public class WebRTCResultsDTO implements Parcelable {
    private long connectionStart;
    private long connectionEnd;
    private long channelOpen;
    private long channelClosed;
    private int exitStatus;

    public WebRTCResultsDTO(){
        connectionStart = System.currentTimeMillis();
        exitStatus = P2PConnectionExitStatus.UNKNOWN.getCode();
    }

    protected WebRTCResultsDTO(Parcel in) {
        connectionStart = in.readLong();
        connectionEnd = in.readLong();
        channelOpen = in.readLong();
        channelClosed = in.readLong();
        exitStatus = in.readInt();
    }

    public static final Creator<WebRTCResultsDTO> CREATOR = new Creator<WebRTCResultsDTO>() {
        @Override
        public WebRTCResultsDTO createFromParcel(Parcel in) {
            return new WebRTCResultsDTO(in);
        }

        @Override
        public WebRTCResultsDTO[] newArray(int size) {
            return new WebRTCResultsDTO[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(connectionStart);
        dest.writeLong(connectionEnd);
        dest.writeLong(channelOpen);
        dest.writeLong(channelClosed);
        dest.writeInt(exitStatus);
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

    public int getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    public void setExitStatus(P2PConnectionExitStatus webRTCExitStatus) {
        this.exitStatus = webRTCExitStatus.getCode();
    }


    @Override
    public String toString() {
        return "WebRTCResultsDTO{" +
                "connectionStart=" + connectionStart +
                ", connectionEnd=" + connectionEnd +
                ", channelOpen=" + channelOpen +
                ", channelClosed=" + channelClosed +
                ", exitStatus=" + exitStatus +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
