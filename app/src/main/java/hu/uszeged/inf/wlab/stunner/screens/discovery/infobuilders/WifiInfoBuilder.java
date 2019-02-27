package hu.uszeged.inf.wlab.stunner.screens.discovery.infobuilders;

import hu.uszeged.inf.wlab.stunner.R;

import java.text.MessageFormat;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

/**
 * Utility class to collect every available information about the WiFi connection.
 * 
 * @author szelezsant
 */
public class WifiInfoBuilder extends AbstractInfoBuilder {

	/**
	 * Constructor.
	 * 
	 * @param context - the {@link Context()} instance to gain access to resources.
	 */
	public WifiInfoBuilder(final Context context) {
		super(context);
	}

	/**
	 * Returns the available information as a {@link String} object.
	 * 
	 * @return the connection info
	 */
	@Override
	public String getInfo() {
		final StringBuilder infoBuilder = new StringBuilder();
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_active_connection),
				getContext().getString(R.string.wifi)));
		final WifiInfo wifiInfo = ((WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
		final String ssidHidden = wifiInfo.getHiddenSSID() ? getContext().getString(R.string.option_does_not_broadcast_ssid) : getContext()
				.getString(R.string.option_broadcasts_ssid);
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_broadcast_ssid), ssidHidden));
		final String ssid = TextUtils.isEmpty(wifiInfo.getSSID()) ? getContext().getString(R.string.unknown) : wifiInfo.getSSID();
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_ssid), ssid));
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_link_speed), wifiInfo.getLinkSpeed()
				+ WifiInfo.LINK_SPEED_UNITS));
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_mac_address), wifiInfo.getMacAddress()));
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_signal_strength), wifiInfo.getRssi()));
		infoBuilder
				.append(MessageFormat.format(getContext().getString(R.string.pattern_local_ip), Parser.parseIp(wifiInfo.getIpAddress())));

		return infoBuilder.toString();
	}

	@Override
	public void fillViewWithInfo(final View layout) {
		final WifiInfo wifiInfo = ((WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
		final TextView connectionType = (TextView) layout.findViewById(R.id.phoneConnectionType);
		final TextView localIp = (TextView) layout.findViewById(R.id.phoneIP);
		final TextView mac = (TextView) layout.findViewById(R.id.phoneMAC);
		final TextView linkspeed = (TextView) layout.findViewById(R.id.phoneLinkSpeed);
		final TextView signalStrength = (TextView) layout.findViewById(R.id.phoneSingalStrength);

		connectionType.setText(R.string.wifi_connected);
		localIp.setText(Parser.parseIp(wifiInfo.getIpAddress()));
		mac.setText(wifiInfo.getMacAddress());
		linkspeed.setText(wifiInfo.getLinkSpeed() + " " + WifiInfo.LINK_SPEED_UNITS);
		signalStrength.setText(wifiInfo.getRssi() + " dB");
	}

	/**
	 * Gets the formatted IPv4 address.
	 * 
	 * @param context - the {@link Context} to gain access to system resources.
	 * @return IP as a String.
	 */
	public static String getIPFormatted(final Context context) {
		return Parser.parseIp(((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress());
	}
}
