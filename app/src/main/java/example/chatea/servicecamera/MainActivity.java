package example.chatea.servicecamera;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * TODO add keyguard when recording. (Cannot leave app when it is recording)
 */
public class MainActivity extends Activity {

    private boolean mRecording;
    private boolean mHandlingEvent;
    private boolean flagClose=false;
    private RadioButton mFrontRadioButton;
    private RadioButton mBackRadioButton;
    private Button mRecordingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("resuming","onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFrontRadioButton = (RadioButton) findViewById(R.id.front_camera_radio_button);
        if (!Util.isCameraExist(Camera.CameraInfo.CAMERA_FACING_FRONT)) {
            mFrontRadioButton.setVisibility(View.GONE);
            mFrontRadioButton.setChecked(false);
        }
        mBackRadioButton = (RadioButton) findViewById(R.id.back_camera_radio_button);
        if (!Util.isCameraExist(Camera.CameraInfo.CAMERA_FACING_BACK)) {
            mBackRadioButton.setVisibility(View.GONE);
            mBackRadioButton.setChecked(false);
        }

        mRecordingButton = (Button) findViewById(R.id.recording_button);
        mRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
//                 android.os.Process.killProcess(android.os.Process.myPid());

            }
        });
        //start recording oncreate
        if (mRecording) {
            stopRecording();
        } else {
            startRecording();
        }
        //amogh go back to facelogin
//        Intent backtoFL=getPackageManager().getLaunchIntentForPackage("com.example.iris.login1");
//        startActivity(backtoFL);
        //amogh end of go back to facelogin

        if (!Util.isCameraExist(this)) {
            mFrontRadioButton.setVisibility(View.GONE);
            mBackRadioButton.setVisibility(View.GONE);
            mRecordingButton.setVisibility(View.GONE);

            TextView noCameraTextView = (TextView) findViewById(R.id.no_camera_text_view);
            noCameraTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("resuming","onResume Called");
        if(flagClose){
            Log.d("resuming","close called");
            Handler mHandler=new Handler();
            Runnable mLaunchTask = new Runnable() {
                public void run() {
                                     android.os.Process.killProcess(android.os.Process.myPid());
                }
            };
            mHandler.postDelayed(mLaunchTask,100);
        }
        flagClose=true;
    }

    private void startRecording() {
        if (!mHandlingEvent) {
            mHandlingEvent = true;
            ResultReceiver receiver = new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    setRecording(true);
                    handleStartRecordingResult(resultCode, resultData);
                    mHandlingEvent = false;
                }
            };
            if (mFrontRadioButton.isChecked()) {
                CameraService.startToStartRecording(this,
                        Camera.CameraInfo.CAMERA_FACING_FRONT, receiver);
            } else if (mBackRadioButton.isChecked()) {
                CameraService.startToStartRecording(this,
                        Camera.CameraInfo.CAMERA_FACING_FRONT, receiver);
            } else {
                throw new IllegalStateException("Must choose a camera for recording");
            }
        }
    }

    private void stopRecording() {
        if (!mHandlingEvent) {
            mHandlingEvent = true;
            ResultReceiver receiver = new ResultReceiver(new Handler()) { // Handler makes sure that the results are not returned in the background service thread, but in the UI thread
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    setRecording(false);
                    handleStopRecordingResult(resultCode, resultData);
                    mHandlingEvent = false;
                }
            };
            CameraService.startToStopRecording(this, receiver);
        }
    }

    private void setRecording(boolean recording) {
        if (recording) {
            mRecording = true;
            mRecordingButton.setText(R.string.stop_recording);
        } else {
            mRecording = false;
            mRecordingButton.setText(R.string.start_recording);
        }
    }

    private void handleStartRecordingResult(int resultCode, Bundle resultData) {
        if (resultCode == CameraService.RECORD_RESULT_OK) {
            Toast.makeText(this, "Start recording...", Toast.LENGTH_SHORT).show();
        } else {
            // start recording failed.
            Toast.makeText(this, "Start recording failed...", Toast.LENGTH_SHORT).show();
            setRecording(false);
        }
    }

    private void handleStopRecordingResult(int resultCode, Bundle resultData) {
        if (resultCode == CameraService.RECORD_RESULT_OK) {
            String videoPath = resultData.getString(CameraService.VIDEO_PATH);
            Toast.makeText(this, "Record succeed, file saved in " + videoPath,
                    Toast.LENGTH_LONG).show();
        } else if (resultCode == CameraService.RECORD_RESULT_UNSTOPPABLE) {
            Toast.makeText(this, "Stop recording failed...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Recording failed...", Toast.LENGTH_SHORT).show();
            setRecording(true);
        }
    }
}
