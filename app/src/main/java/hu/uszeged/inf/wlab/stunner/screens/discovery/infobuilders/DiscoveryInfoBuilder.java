package hu.uszeged.inf.wlab.stunner.screens.discovery.infobuilders;

import hu.uszeged.inf.wlab.stunner.R;
import hu.uszeged.inf.wlab.stunner.utils.enums.NatDiscoveryResult;

import java.text.MessageFormat;

import android.content.Context;
import android.view.View;
import de.javawi.jstun.test.DiscoveryInfo;

/**
 * Utility class to gain the information from the supplied {@link DiscoveryInfo} object.
 * 
 * @author szelezsant
 */
public class DiscoveryInfoBuilder extends AbstractInfoBuilder {

	/** The source of information. */
	private final DiscoveryInfo discoveryInfo;
	/** The resultCode corresponding to the proper {@link NatDiscoveryResult}. */
	private int resultCode = NatDiscoveryResult.UNKNOWN.getCode();

	/**
	 * Constructor.
	 * 
	 * @param info - the source of information.
	 * @param context - the {@link Context} object to gain access to system resources.
	 */
	public DiscoveryInfoBuilder(final DiscoveryInfo info, final Context context) {
		super(context);
		discoveryInfo = info;
	}

	@Override
	public String getInfo() {
		final StringBuilder infoBuilder = new StringBuilder();
		/* in case of error */
		if (discoveryInfo == null || discoveryInfo.isError()) {
			infoBuilder.append(getContext().getString(R.string.error));
			resultCode = NatDiscoveryResult.ERROR.getCode();
			/* in case of success */
		} else {
			infoBuilder.append(MessageFormat.format(getContext().getString(R.string.pattern_public_ip),
					null == discoveryInfo.getPublicIP()
							? getContext().getString(R.string.n_a)
							: discoveryInfo.getPublicIP().getHostAddress()));
			if (discoveryInfo.isBlockedUDP()) {
				infoBuilder.append(getContext().getString(R.string.firewall_blocks));
				resultCode = NatDiscoveryResult.FIREWALL_BLOCKS.getCode();
			} else if (discoveryInfo.isSymmetricUDPFirewall()) {
				infoBuilder.append(getContext().getString(R.string.symmetric_firewall));
				resultCode = NatDiscoveryResult.SYMMETRIC_FIREWALL.getCode();
			} else if (discoveryInfo.isOpenAccess()) {
				infoBuilder.append(getContext().getString(R.string.open_access));
				resultCode = NatDiscoveryResult.OPEN_ACCESS.getCode();
			} else {
				infoBuilder.append(getNATInfo());
			}
		}
		return infoBuilder.toString();
	}

	/**
	 * Returns the obtained NAT information and sets the resultCode attribute.
	 * 
	 * @return NAT information
	 */
	private String getNATInfo() {
		final String natConnection = getContext().getString(R.string.pattern_handles_connection);

		if (discoveryInfo.isFullCone()) {
			resultCode = NatDiscoveryResult.FULL_CONE.getCode();
			return MessageFormat.format(natConnection, getContext().getString(R.string.full));
		} else if (discoveryInfo.isPortRestrictedCone()) {
			resultCode = NatDiscoveryResult.PORT_RESTRICTED_CONE.getCode();
			return MessageFormat.format(natConnection, getContext().getString(R.string.port_restricted));
		} else if (discoveryInfo.isRestrictedCone()) {
			resultCode = NatDiscoveryResult.RESTRICTED_CONE.getCode();
			return MessageFormat.format(natConnection, getContext().getString(R.string.restricted));
		} else {
			resultCode = NatDiscoveryResult.SYMMETRIC_CONE.getCode();
			return MessageFormat.format(natConnection, getContext().getString(R.string.symmetric));
		}
	}

	/**
	 * Gets the corresponding result code of the proper {@link NatDiscoveryResult}. You should call this method AFTER the
	 * {@link DiscoveryInfoBuilder#getInfo()} method.
	 * 
	 * @return code.
	 */
	public int getDiscoveryResultCode() {
		return resultCode;
	}

	@Override
	public void fillViewWithInfo(final View layout) {
		// TODO Auto-generated method stub

	}

}
