package hu.uszeged.inf.wlab.stunner.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hu.uszeged.inf.wlab.stunner.sync.communication.Connector;
import hu.uszeged.inf.wlab.stunner.sync.communication.request.Record;
import hu.uszeged.inf.wlab.stunner.sync.communication.request.UploadRequest;
import hu.uszeged.inf.wlab.stunner.sync.communication.response.UploadResponse;
import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.GeneralResource;

public class SaveRecordToServer extends JobIntentService {
    public static final String TAG = "SaveRecordToServer";

    /**
     * Unique job ID for this service.
     */
    public static final int JOB_ID = 7288165;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SaveRecordToServer.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final Calendar cal = Calendar.getInstance();
        if (preferences.getInt(Constants.LAST_REFRESH_YEAR, 0) != cal.get(Calendar.YEAR) ||
                preferences.getInt(Constants.LAST_REFRESH_MONTH, 0) != cal.get(Calendar.MONTH) ||
                preferences.getInt(Constants.LAST_REFRESH_DAY, 0) != cal.get(Calendar.DAY_OF_MONTH)) {
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(Constants.LAST_REFRESH_YEAR, cal.get(Calendar.YEAR));
            editor.putInt(Constants.LAST_REFRESH_MONTH, cal.get(Calendar.MONTH));
            editor.putInt(Constants.LAST_REFRESH_DAY, cal.get(Calendar.DAY_OF_MONTH));
            editor.putInt(Constants.TODAY_UPLOADED_RECORD_COUNT, 0);
            editor.commit();
            deleteOldRecords();
        }
        ArrayList<String> discoveryDtoList = new ArrayList<String>();
        if(intent.hasExtra(Constants.KEY_DISCOVERY_DTO_LIST)){
            discoveryDtoList.addAll(intent.getStringArrayListExtra(Constants.KEY_DISCOVERY_DTO_LIST));
        }
        String discovery = Constants.PREF_STRING_VALUE_EMPTY;
        if(intent.hasExtra(Constants.KEY_DISCOVERY_DTO)) {
            discovery = intent.getStringExtra(Constants.KEY_DISCOVERY_DTO);
            discoveryDtoList.add(discovery);
        }
        final boolean isOnline = isOnlineStatus();
        if(intent.hasExtra(Constants.KEY_P2P_RESULTS)){
            final String p2pResults =  intent.getStringExtra(Constants.KEY_P2P_RESULTS);
            saveRecordParse(p2pResults,discovery);
        }
        if(!isOnline) {
            saveRecordToLocalParseServer(discoveryDtoList);
        } else {
            ArrayList<String> remainingRecords = sendRecordToFictServer(discoveryDtoList);
            Log.d(TAG, ""+remainingRecords.size());
            if(remainingRecords.size()>0) {
                saveRecordToLocalParseServer(discoveryDtoList);
            }
        }
    }

    private void saveRecordParse(String p2pResults, String discovery) {
        ParseObject measurement = new ParseObject(Constants.REMOTE_PARSE_SERVER_TABLE_NAME);
        measurement.put(Constants.KEY_DISCOVERY_DTO, discovery);
        measurement.put(Constants.KEY_LOCAL_ANDROID_ID,Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        measurement.put(Constants.KEY_P2P_RESULTS,p2pResults);
        measurement.put(Constants.KEY_TIMESTAMP,System.currentTimeMillis());
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean(Constants.PREF_KEY_PARSE_SERVICE, true)) {
            try {
                if(isOnlineStatus()){
                    measurement.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d(TAG, "SAVE - I saved it online PARSE database");
                        }
                    });
                } else {
                    measurement.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d(TAG, "SAVE - I saved it local datastore");
                        }
                    });
                }

            } catch(Exception ex) {
                Log.d(TAG, "Error during save in Parse");
            }
        } else {
            Log.d(TAG, "No authority for save in Parse");
        }
    }

    private ArrayList<String> sendRecordToFictServer(ArrayList<String> discoveryList){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int indexOfLastSuccessfulSaved = -1;
        for (int i=0; i<discoveryList.size();i++) {
            List<Record> recordList = new ArrayList<Record>();
            recordList.add(new Record(discoveryList.get(i)));
            if (preferences.getInt(Constants.TODAY_UPLOADED_RECORD_COUNT, 0) < Constants.MAX_DAILY_UPLOAD_LIMIT) {
                final UploadResponse response = new Connector(this).post(new UploadRequest(GeneralResource.createHashedId(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)), recordList));
                if (response.getResult().isSuccess()) {
                    Log.d(TAG, "Uploading records to FICT server was successful.");
                    final SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt(Constants.TODAY_UPLOADED_RECORD_COUNT, preferences.getInt(Constants.TODAY_UPLOADED_RECORD_COUNT, 0) + 1);
                    editor.commit();
                    Log.d(TAG, "Today uploaded record count: " + String.valueOf(preferences.getInt(Constants.TODAY_UPLOADED_RECORD_COUNT, 0)));
                    indexOfLastSuccessfulSaved = i;
                } else {
                    Log.d(TAG, "Response contains error: " + response.getResult().getErrorCode().name() + " response:" + response.toString());
                    break;
                }
            } else {
                break;
            }
        }
        ArrayList<String> returnList = new ArrayList<String>();
        if(indexOfLastSuccessfulSaved+1 < discoveryList.size()) {
            for (int j = indexOfLastSuccessfulSaved+1; j < discoveryList.size(); j++) {
                returnList.add(discoveryList.get(j));
            }
        }
        return returnList;
    }

    private void saveRecordToLocalParseServer(ArrayList<String> discoveryList) {
        for (String discovery: discoveryList) {
            ParseObject parseobjectForFictServer = new ParseObject(Constants.FICT_SERVER_LOCAL_PARSE_TABLE_NAME);
            parseobjectForFictServer.put(Constants.KEY_DISCOVERY_DTO,discovery);
            parseobjectForFictServer.put(Constants.KEY_TIMESTAMP,System.currentTimeMillis());
            parseobjectForFictServer.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.d(TAG, "SAVE - I saved it to local datastore");
                }
            });
        }
    }

    /**
     * Delete the old discoveries from the database.
     */
    private void deleteOldRecords() {
        List<String> parseDatabaseNameList = new ArrayList<String>();
        parseDatabaseNameList.add(Constants.FICT_SERVER_LOCAL_PARSE_TABLE_NAME);
        parseDatabaseNameList.add(Constants.REMOTE_PARSE_SERVER_TABLE_NAME);
        for (String parseDatabaseName:parseDatabaseNameList) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery(parseDatabaseName);
            query.fromLocalDatastore();
            query.ignoreACLs();
            final Context context = this;
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> measurementList, ParseException e) {
                    if (e == null) {
                        Log.d(TAG, "deleteOldRecords, measurementList.size:" + measurementList.size());
                        for (ParseObject measurement : measurementList) {
                            Long timestamp = measurement.getLong(Constants.KEY_TIMESTAMP);
                            if(timestamp<System.currentTimeMillis() - (30 * Constants.MILLISEC_TO_DAY_RATIO)){
                                measurement.unpinInBackground();
                            }
                        }
                    } else {
                        Log.d(TAG, "Error reading from local PARSE datastore"+e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
    }

    public boolean isOnlineStatus() {
        NetworkInfo netInfo =  ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
