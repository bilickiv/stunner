package hu.uszeged.inf.wlab.stunner.service;

/**
 * Created by teglaskrisztian on 2018. 04. 11..
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;

import hu.uszeged.inf.wlab.stunner.screens.discovery.MainActivity;
import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.dtos.P2PResultsDTO;
import hu.uszeged.inf.wlab.stunner.utils.enums.P2PConnectionExitStatus;

public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        final String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if(refreshedToken == null){
            Long startTimeStamp = System.currentTimeMillis();
            while (FirebaseInstanceId.getInstance().getToken() == null) {
                try {
                    Thread.sleep(1 * Constants.MILLISEC_TO_SECOND_RATIO);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (System.currentTimeMillis() - startTimeStamp > 30 * Constants.MILLISEC_TO_SECOND_RATIO) {
                    break;
                }
            }
            refreshedToken = FirebaseInstanceId.getInstance().getToken();
            if (refreshedToken == null) {
                Log.d(TAG, "Device token is null after 30 seconds");
                return;
            }
        }
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child("tokenID").setValue(refreshedToken);
        Log.d(TAG, "Refreshed token: " + refreshedToken );
    }
}
