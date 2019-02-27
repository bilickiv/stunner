package hu.uszeged.inf.wlab.stunner.screens.discovery.infobuilders;

import android.content.Context;

/**
 * Ascendant info builder class.
 * 
 * @author szelezsant
 */
public abstract class AbstractInfoBuilder implements InfoBuilderInterface {

	/** The {@link Context} instance to gain access to system resources. */
	private final Context context;

	/**
	 * Constructor.
	 * 
	 * @param context - the context to gain data from.
	 */
	public AbstractInfoBuilder(final Context context) {
		this.context = context;
	}

	/**
	 * Returns the {@link Context} instance.
	 * 
	 * @return context.
	 */
	protected Context getContext() {
		return context;
	}

}
