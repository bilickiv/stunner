package hu.uszeged.inf.wlab.stunner.service.triggerjobservice;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.PersistableBundle;
import android.util.Log;

import hu.uszeged.inf.wlab.stunner.service.StateManagerService;
import hu.uszeged.inf.wlab.stunner.utils.Constants;

@TargetApi(21)
public class StateManagerServiceTriggerJobService extends JobService {
    private static final String TAG = "StateManagerTriggerJS";

    @Override
    public boolean onStartJob(JobParameters params) {
        Intent intent = new Intent(StateManagerServiceTriggerJobService.this, StateManagerService.class);
        PersistableBundle bundle = params.getExtras();
        if(bundle != null){
            intent.setAction(bundle.getString(Constants.KEY_STATE,Constants.ACTION_STATE_CHECK));
        } else {
            intent.setAction(Constants.ACTION_STATE_CHECK);
        }
        StateManagerService.enqueueWork(this,intent);
        Log.d(TAG,"startStateManagerService!");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG,"onStopJob");
        return false;
    }
}
