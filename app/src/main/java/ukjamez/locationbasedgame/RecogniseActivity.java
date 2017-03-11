package ukjamez.locationbasedgame;

import android.app.IntentService;
import android.content.Intent;

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

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        for( DetectedActivity activity : result.getProbableActivities() ) {
            switch( activity.getType() ) {
                //case DetectedActivity.IN_VEHICLE: {
                    //Log.e( "ActivityRecogition", "In Vehicle: " + activity.getConfidence() );
                //    break;
                //}
                //case DetectedActivity.ON_BICYCLE: {
                    //Log.e( "ActivityRecogition", "On Bicycle: " + activity.getConfidence() );
                //    break;
                //}
                case DetectedActivity.ON_FOOT: {
                    //Log.e( "ActivityRecogition", "On Foot: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.RUNNING: {
                    //Log.e( "ActivityRecogition", "Running: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.STILL: {
                    //Log.e( "ActivityRecogition", "Still: " + activity.getConfidence() );
                    break;
                }
                //case DetectedActivity.TILTING: {
                    //Log.e( "ActivityRecogition", "Tilting: " + activity.getConfidence() );
                //    break;
                //}
                case DetectedActivity.WALKING: {
                    //Log.e( "ActivityRecogition", "Walking: " + activity.getConfidence() );
                    if( activity.getConfidence() >= 75 ) {
                        //NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        //builder.setContentText( "Are you walking?" );
                       //builder.setSmallIcon( R.mipmap.ic_launcher );
                        //builder.setContentTitle( getString( R.string.app_name ) );
                        //NotificationManagerCompat.from(this).notify(0, builder.build());
                    }
                    break;
                }
                case DetectedActivity.UNKNOWN: {

                    break;
                }
            }
        }}}
}
