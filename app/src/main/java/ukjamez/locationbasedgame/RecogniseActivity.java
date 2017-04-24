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
    }

    public String CurrentActivityName = "checking";
    public String LastDifferentActivity = "N/A";
    private static final String PrefsFile = "PrefsFile";

    @Override
    protected void onHandleIntent(Intent intent) {

        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            DetectedActivity mostLikelyActivity = result.getMostProbableActivity();
            int walkingConfidence = result.getActivityConfidence(7);
            int runningConfidence = result.getActivityConfidence(8);

            if(mostLikelyActivity.getType() == DetectedActivity.ON_FOOT && runningConfidence >= walkingConfidence){
                if(result.getActivityConfidence(0) < 40 ){
                    if (LastDifferentActivity == "Driving"){
                        CurrentActivityName = "Driving";
                    }else{
                        CurrentActivityName = "Running";
                    }
                }
            } else if (mostLikelyActivity.getType() == DetectedActivity.ON_FOOT){
                if (LastDifferentActivity == "Driving"){
                    CurrentActivityName = "Driving";
                }else{
                    CurrentActivityName = ("Walking");
                }
            } else if (mostLikelyActivity.getType() == DetectedActivity.ON_BICYCLE){
                CurrentActivityName = ("Cycling");
            } else if (mostLikelyActivity.getType() == DetectedActivity.IN_VEHICLE){
                CurrentActivityName = ("Driving");
            } else if (mostLikelyActivity.getType() == DetectedActivity.STILL){
                CurrentActivityName = ("Still");
            } else{
                CurrentActivityName = ("Other");
            }


            if (CurrentActivityName != LastDifferentActivity){
                LastDifferentActivity = CurrentActivityName;
            }

                SharedPreferences pref = getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.putString("testConfidence", CurrentActivityName + "/d/" + result.getActivityConfidence(0) + "/r/" + result.getActivityConfidence(8));
                editor.putString("currentActivity", CurrentActivityName);
                editor.commit();

        }
    }
}
