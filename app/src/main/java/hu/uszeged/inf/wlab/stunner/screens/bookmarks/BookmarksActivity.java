package hu.uszeged.inf.wlab.stunner.screens.bookmarks;

import hu.uszeged.inf.wlab.stunner.R;

import hu.uszeged.inf.wlab.stunner.screens.bookmarks.adapter.BookmarkAdapter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class BookmarksActivity extends AppCompatActivity {

	BookmarkAdapter bookmarkAdapter;
	int runCounter = 0;
	private List<ParseObject> books = new ArrayList<>();
	private Menu menu;
	DeleteBookmark delete = new DeleteBookmark();
	AdapterView<?> parent;
	private boolean pushSaveMeasurement;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* initialize the UI */
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_bookmarks);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		final ListView listView = (ListView) findViewById(R.id.listViewBookmarks);
		listView.setAdapter(bookmarkAdapter);
		listView.setSelected(true);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = preferences.edit();
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, final View row, final int position, final long identifier) {
				Log.d("BookmarksActivity", "onItemLongClick: position=" + position + ", id=" + identifier);
				final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						if (DialogInterface.BUTTON_POSITIVE == which) {

							books.remove(position);
							((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();

							delete.deleteBookmark(position, BookmarksActivity.this);
							pushSaveMeasurement = false;
							editor.putBoolean("pushSaveMeasurement", pushSaveMeasurement);
							editor.commit();
						}
						dialog.dismiss();
					}
				};
				new AlertDialog.Builder(BookmarksActivity.this).setTitle(R.string.warning).setMessage(R.string.delete_prompt)
						.setNegativeButton(android.R.string.no, listener).setPositiveButton(android.R.string.yes, listener).create().show();
				setParent(parent);
				return true;
			}
		});

		listView.setEmptyView(findViewById(R.id.textViewEmptyLabel));

		ParseQuery<ParseObject> query = ParseQuery.getQuery("SavedMeasurements");
		query.fromLocalDatastore();
		query.ignoreACLs();
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> bookmarkList,
							 ParseException e) {
				if (e == null) {
					for (ParseObject bookmark : bookmarkList) {
						//bookmark.unpinInBackground();
						listView.setAdapter(bookmarkAdapter);
						bookmarkAdapter = new BookmarkAdapter(bookmarkList);
						if(runCounter==0){
							listView.setAdapter(bookmarkAdapter);
							bookmarkAdapter = new BookmarkAdapter(bookmarkList);
							runCounter++;
						}
						books = bookmarkList;
					}
				} else {
					Log.d("BookmarksActivity", "Error reading from local PARSE datastore"+e.getMessage());
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu newMenu) {
		getMenuInflater().inflate(R.menu.saved_measurements, newMenu);
		this.menu = newMenu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (R.id.deleted_all_item == item.getItemId() && books.size()!=0) {

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			final SharedPreferences.Editor editor = preferences.edit();


			final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					if (DialogInterface.BUTTON_POSITIVE == which) {

						books.clear();
						delete.deleteAllBookmarks();
						final ListView listView = (ListView) findViewById(R.id.listViewBookmarks);
						listView.setAdapter(bookmarkAdapter);
						bookmarkAdapter = new BookmarkAdapter(books);
						//finish();
						pushSaveMeasurement = false;
						editor.putBoolean("pushSaveMeasurement", pushSaveMeasurement);
						editor.commit();
					}
					dialog.dismiss();
				}
			};
			new AlertDialog.Builder(BookmarksActivity.this).setTitle(R.string.warning).setMessage(R.string.deleted_all)
					.setNegativeButton(android.R.string.no, listener).setPositiveButton(android.R.string.yes, listener).create().show();

			return true;
		} else {
			final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					if (DialogInterface.BUTTON_POSITIVE == which) { }
					dialog.dismiss();
				}
			};
			new AlertDialog.Builder(BookmarksActivity.this).setTitle(R.string.warning).setMessage(R.string.empty_view)
					.setPositiveButton(android.R.string.yes, listener).create().show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	public void setParent(AdapterView<?> parent){
		this.parent = parent;
	}
}