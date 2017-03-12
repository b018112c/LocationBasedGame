package ukjamez.locationbasedgame;

import android.app.IntentService;
import android.content.Intent;
import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by James on 11/03/2017.
 */

public class RecogniseActivity extends IntentService {

    public RecogniseActivity() {
        super("ActivityRecognizedService");
        //textTestActivity = (TextView) findViewById(R.id.textTest);
    }

    public RecogniseActivity(String name) {
        super(name);
    }

    public TextView textTestActivity;

    @Override
    protected void onHandleIntent(Intent intent) {

        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            DetectedActivity mostLikelyActivity = result.getMostProbableActivity();
            int walkingConfidence = result.getActivityConfidence(7);
            int runningConfidence = result.getActivityConfidence(8);

            if(mostLikelyActivity.getType() == DetectedActivity.ON_FOOT && runningConfidence >= walkingConfidence){
                //textTestActivity.setText("Running");
            //    mainActivity.SetTestText("Running");
            } else if (mostLikelyActivity.getType() == DetectedActivity.ON_FOOT){
            //    mainActivity.SetTestText("Walking");
            } else if (mostLikelyActivity.getType() == DetectedActivity.ON_BICYCLE){
            //    mainActivity.SetTestText("Running"); //for now
            } else{
            //    mainActivity.SetTestText("Other");
            }


        }
    }
}
