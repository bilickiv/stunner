package hu.uszeged.inf.wlab.stunner.screens.discovery.infobuilders;

import android.view.View;

/**
 * Interface to define mandatory method declarations used in the builders.
 * 
 * @author szelezsant
 */
public interface InfoBuilderInterface {

	/**
	 * Gets the acquired discovery information.
	 * 
	 * @return info
	 */
	public String getInfo();

	/**
	 * @param layout screen layout.
	 */
	public void fillViewWithInfo(final View layout);

}
