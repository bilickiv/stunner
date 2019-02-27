package hu.uszeged.inf.wlab.stunner.screens.bookmarks.adapter;

import hu.uszeged.inf.wlab.stunner.R;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseObject;


public class BookmarkAdapter extends BaseAdapter {

	private final List<ParseObject> bookmarks;

	public BookmarkAdapter(final List <ParseObject> bookmarks) {
		super();
		this.bookmarks = bookmarks;
	}

	@Override
	public int getCount() {
		return bookmarks.size();
	}

	@Override
	public Object getItem(final int position) {
		return bookmarks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		RelativeLayout returnView = (RelativeLayout) convertView;
		final ParseObject bookmark = bookmarks.get(position);
		int conMod = bookmark.getInt("connInterface");
		if (null == returnView) {
			returnView = (RelativeLayout) ((LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
					R.layout.bookmark_row, parent, false);
			final ViewHolder holder = new ViewHolder();
			holder.contentTextViewLocalIP = (TextView) returnView.findViewById(R.id.textViewLocalIP);
			holder.contentTextViewPublicIP = (TextView) returnView.findViewById(R.id.textViewPublicIP);
			holder.contetnTextViewDate = (TextView) returnView.findViewById(R.id.textViewDate);
			holder.contentTextViewResult = (TextView) returnView.findViewById(R.id.textViewResult);
			if(conMod == 0){
				holder.contentTextViewProvider = (TextView) returnView.findViewById(R.id.textViewProvider);
				holder.labelTextViewProvider = (TextView) returnView.findViewById(R.id.textViewCarrierT);
				holder.labelTextViewProvider.setVisibility(View.VISIBLE);
				holder.contentTextViewNetwork = (TextView) returnView.findViewById(R.id.textViewNetwork);
				holder.labelTextViewNetwork = (TextView) returnView.findViewById(R.id.textViewNetworkT);
				holder.labelTextViewNetwork.setVisibility(View.VISIBLE);
			} else {
				holder.contentTextViewSsid = (TextView) returnView.findViewById(R.id.textViewSsid);
				holder.labelTextViewSsid = (TextView) returnView.findViewById(R.id.textViewSsidT);
				holder.labelTextViewSsid.setVisibility(View.VISIBLE);
			}
			holder.contentTextViewConnectionInterface = (TextView) returnView.findViewById(R.id.textViewConnectionInterface);
			returnView.setTag(holder);
		}

		final ViewHolder holder = (ViewHolder) returnView.getTag();
		holder.contetnTextViewDate.setText(bookmark.getString("date"));
		holder.contentTextViewResult.setText(bookmark.getString("result"));
		holder.contentTextViewLocalIP.setText(bookmark.getString("localIP"));
		holder.contentTextViewPublicIP.setText(bookmark.getString("publicIP"));
		if (conMod == 0){
			holder.contentTextViewProvider.setText(bookmark.getString("provider"));
			holder.contentTextViewProvider.setVisibility(View.VISIBLE);
			holder.contentTextViewNetwork.setText(bookmark.getString("network"));
			holder.contentTextViewNetwork.setVisibility(View.VISIBLE);
			holder.contentTextViewConnectionInterface.setText(R.string.mobile_data_connected);
		} else {
			holder.contentTextViewSsid.setText(bookmark.getString("ssid"));
			holder.contentTextViewSsid.setVisibility(View.VISIBLE);
			holder.contentTextViewConnectionInterface.setText(R.string.wifi_connected);
		}


		return returnView;
	}

	static class ViewHolder {
		/** TextView to display the date. */
		private TextView contetnTextViewDate;
		/** TextView to display the address. */
		private TextView contentTextViewAddress;
		/** TextView to display the discovery result. */
		private TextView contentTextViewResult;
		/** TextView to display the local IP. */
		private TextView contentTextViewLocalIP;
		/** TextView to display the public IP. */
		private TextView contentTextViewPublicIP;

		private TextView contentTextViewProvider;
		private TextView labelTextViewProvider;

		private TextView contentTextViewConnectionInterface;

		private TextView contentTextViewNetwork;
		private TextView labelTextViewNetwork;


		private TextView contentTextViewSsid;
		private TextView labelTextViewSsid;

	}

}