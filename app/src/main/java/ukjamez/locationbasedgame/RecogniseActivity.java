package ukjamez.locationbasedgame;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by James on 11/03/2017.
 */

public class RecogniseActivity extends IntentService {

    public RecogniseActivity() {
        super("ActivityRecognizedService");

    }

    public RecogniseActivity(String name) {
        super(name);
        //textTestActivity = (TextView) textTestActivity.findViewById(R.id.textTest);
    }

    //public TextView textTestActivity;

    public String CurrentActivityName = "checking";
    private static final String PrefsFile = "PrefsFile";

    @Override
    protected void onHandleIntent(Intent intent) {

        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            DetectedActivity mostLikelyActivity = result.getMostProbableActivity();
            int walkingConfidence = result.getActivityConfidence(7);
            int runningConfidence = result.getActivityConfidence(8);

            if(mostLikelyActivity.getType() == DetectedActivity.ON_FOOT && runningConfidence >= walkingConfidence){
                //textTestActivity.setText("Running");
                CurrentActivityName = "Running";
            } else if (mostLikelyActivity.getType() == DetectedActivity.ON_FOOT){
                CurrentActivityName = ("Walking");
            } else if (mostLikelyActivity.getType() == DetectedActivity.ON_BICYCLE){
                CurrentActivityName = ("Running"); //for now
            } else{
                CurrentActivityName = ("Other");
            }

                SharedPreferences pref = getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                //String val = pref.getString("currentActivity", "N/A");
                editor.putString("currentActivity", CurrentActivityName);
                editor.commit();

        }
    }
}
