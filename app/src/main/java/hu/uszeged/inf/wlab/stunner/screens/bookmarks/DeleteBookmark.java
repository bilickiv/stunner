package hu.uszeged.inf.wlab.stunner.screens.bookmarks;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class DeleteBookmark {

    ParseQuery<ParseObject> query = ParseQuery.getQuery("SavedMeasurements");

    public void deleteBookmark(final int pos, final Context context){
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.findInBackground(new FindCallback<ParseObject>() {
            int id = 0;
            public void done(List<ParseObject> bookmarkList,
                             ParseException e) {
                if (e == null) {
                    for (ParseObject bookmark : bookmarkList) {
                        if(id==pos){
                            bookmark.unpinInBackground();
                            Toast.makeText(context, "Deleted from bookmarks!",
                                    Toast.LENGTH_LONG).show();
                        }
                        id++;
                    }
                } else {
                    Log.d("DeleteBookmark", "Error deleting from local PARSE datastore"+e.getMessage());
                }
            }
        });
    }

    public void deleteAllBookmarks(){
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> bookmarkList,
                             ParseException e) {
                if (e == null) {
                    for (ParseObject bookmark : bookmarkList) {
                        bookmark.unpinInBackground();
                    }
                } else {
                    Log.d("DeleteBookmark", "Error deleting from local PARSE datastore"+e.getMessage());
                }
            }
        });
    }
    //----------------------------------MODIFIED
    public void deleteLastBookmark(final Context context) {
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.findInBackground(new FindCallback<ParseObject>() {
            int id = 1;
            int size = 0;
            public void done(List<ParseObject> bookmarkList,
                             ParseException e) {
                if (e == null) {
                    size = bookmarkList.size();
                    for (ParseObject bookmark : bookmarkList) {
                        if (id == size) {
                            bookmark.unpinInBackground();
                        }
                        id++;
                    }
                } else {
                    Log.d("DeleteBookmark", "Error deleting from local PARSE datastore" + e.getMessage());
                }
            }
        });
    }
    //----------------------------------------------
}