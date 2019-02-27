package hu.uszeged.inf.wlab.stunner.screens.discovery.infobuilders;

import hu.uszeged.inf.wlab.stunner.R;
import hu.uszeged.inf.wlab.stunner.utils.enums.MobileNetType;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.Enumeration;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Utility class to collect every available information about the mobile
 * internet connection.
 * 
 * @author szelezsant
 */
public class MobileNetworkInfoBuilder extends AbstractInfoBuilder {

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            - the {@link Context} instance to gain access to system
	 *            resources.
	 */
	public MobileNetworkInfoBuilder(final Context context) {
		super(context);
	}

	@Override
	public String getInfo() {
		final StringBuilder infoBuilder = new StringBuilder();
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_active_connection), getContext().getString(R.string.mobile)));
		final TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_data_connection),
				MobileNetType.getByCode(telephonyManager.getNetworkType()).getName()));
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_roaming), Boolean.toString(telephonyManager.isNetworkRoaming())));
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_carrier), telephonyManager.getNetworkOperatorName()));
		infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_local_ip), getIPFormatted(getContext())));

		return infoBuilder.toString();
	}

	@Override
	public void fillViewWithInfo(final View layout) {
		final TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		final TextView connectionType = (TextView) layout.findViewById(R.id.phoneConnectionType);
		final TextView localIp = (TextView) layout.findViewById(R.id.phoneIP);
		final TextView carrier = (TextView) layout.findViewById(R.id.phoneCarrier);
		final TextView type = (TextView) layout.findViewById(R.id.phoneDataConnectionType);

		connectionType.setText(MobileNetType.getByCode(telephonyManager.getNetworkType()).getName());
		localIp.setText(getIPFormatted(getContext()));
		carrier.setText(telephonyManager.getNetworkOperatorName());
		type.setText(MobileNetType.getByCode(telephonyManager.getNetworkType()).getName());
	}

	/**
	 * Returns the IPv4 address from the network interface, if possible, or the
	 * "unknown" string.
	 * 
	 * @param context
	 *            - the {@link Context} object to gain access to system
	 *            resources.
	 * @return IPv4 address.
	 */
	public static String getIPFormatted(final Context context) {
		try {
			for (final Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				final NetworkInterface intf = en.nextElement();
				for (final Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					final InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.getHostAddress().contains(":")) {
						Log.v(MobileNetworkInfoBuilder.class.getSimpleName(), "Selected IP address: " + inetAddress.getHostAddress());
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (final SocketException socketException) {
			Log.e(MobileNetworkInfoBuilder.class.getSimpleName(), "getIPFormatted: exception ocurred when getting ip" + socketException);
		}
		return context.getString(R.string.unknown);
	}
}
