package hu.uszeged.inf.wlab.stunner.view;

import hu.uszeged.inf.wlab.stunner.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Custom action View to represent a toggle button which reflects the marked state of the current discovery.
 * 
 * @author szelezsant
 */
public class BookmarkActionView extends RelativeLayout {

	/** Flag to indicate if the discovery is marked. */
	private boolean marked;

	/**
	 * Constructor.
	 * 
	 * @param context - the calling context.
	 */
	public BookmarkActionView(final Context context) {
		super(context);
		inflate(context, R.layout.view_bookmark, this);
	}

	/**
	 * Advanced constructor. Allows that the view could be instantiated from xml.
	 * 
	 * @param context - the calling context.
	 * @param attrs - the attributes from the xml.
	 */
	public BookmarkActionView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.view_bookmark, this);
	}

	/**
	 * Gets the marked state.
	 * 
	 * @return marked
	 */
	public boolean isMarked() {
		return marked;
	}

	/**
	 * Sets the marked state.
	 * 
	 * @param marked - state to set
	 */
	public void setMarked(final boolean marked) {
		if (this.marked != marked) {
			this.marked = marked;
			((ImageView) findViewById(R.id.imageViewMarker)).setImageResource(marked
					? android.R.drawable.star_on
					: android.R.drawable.star_off);
		}
	}
}
