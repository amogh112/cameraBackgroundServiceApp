package example.chatea.servicecamera;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraService extends Service {
    private static final String TAG = CameraService.class.getSimpleName();

    public static final String RESULT_RECEIVER = "resultReceiver";
    public static final String VIDEO_PATH = "recordedVideoPath";

    public static final int RECORD_RESULT_OK = 0;
    public static final int RECORD_RESULT_DEVICE_NO_CAMERA= 1;
    public static final int RECORD_RESULT_GET_CAMERA_FAILED = 2;
    public static final int RECORD_RESULT_ALREADY_RECORDING = 3;
    public static final int RECORD_RESULT_NOT_RECORDING = 4;
    public static final int RECORD_RESULT_UNSTOPPABLE = 5;

    private static final String START_SERVICE_COMMAND = "startServiceCommands";
    private static final int COMMAND_NONE = -1;
    private static final int COMMAND_START_RECORDING = 0;
    private static final int COMMAND_STOP_RECORDING = 1;

    private static final String SELECTED_CAMERA_FOR_RECORDING = "cameraForRecording";

    private Camera mCamera;
    private MediaRecorder mMediaRecorder;

    private boolean mRecording = false;
    private String mRecordingPath = null;

    public CameraService() {
    }

    public static void startToStartRecording(Context context, int cameraId,
                                             ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, CameraService.class);
        intent.putExtra(START_SERVICE_COMMAND, COMMAND_START_RECORDING);
        intent.putExtra(SELECTED_CAMERA_FOR_RECORDING, cameraId);
        intent.putExtra(RESULT_RECEIVER, resultReceiver);
        context.startService(intent);
    }

    public static void startToStopRecording(Context context, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, CameraService.class);
        intent.putExtra(START_SERVICE_COMMAND, COMMAND_STOP_RECORDING);
        intent.putExtra(RESULT_RECEIVER, resultReceiver);
        context.startService(intent);
    }

    /**
     * Used to take picture.
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = Util.getOutputMediaFile(Util.MEDIA_TYPE_IMAGE);

            if (pictureFile == null) {
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            throw new IllegalStateException("Must start the service with intent");
        }
        switch (intent.getIntExtra(START_SERVICE_COMMAND, COMMAND_NONE)) {
            case COMMAND_START_RECORDING:
                handleStartRecordingCommand(intent);
                break;
            case COMMAND_STOP_RECORDING:
                handleStopRecordingCommand(intent);
                break;
            default:
                throw new UnsupportedOperationException("Cannot start service with illegal commands");
        }

        return START_NOT_STICKY;
    }

    private void handleStartRecordingCommand(Intent intent) {
        if (!Util.isCameraExist(this)) {
            throw new IllegalStateException("There is no device, not possible to start recording");
        }

        final ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);

        if (mRecording) {
            // Already recording
            resultReceiver.send(RECORD_RESULT_ALREADY_RECORDING, null);
            return;
        }
        mRecording = true;


        final int cameraId = intent.getIntExtra(SELECTED_CAMERA_FOR_RECORDING,
                Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera = Util.getCameraInstance(cameraId);
        if (mCamera != null) {
            SurfaceView sv = new SurfaceView(this);

            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);

            SurfaceHolder sh = sv.getHolder();

            sv.setZOrderOnTop(true);
            sh.setFormat(PixelFormat.TRANSPARENT);

            sh.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    Camera.Parameters params = mCamera.getParameters();
                    mCamera.setParameters(params);
                    Camera.Parameters p = mCamera.getParameters();

                    List<Camera.Size> listSize;
                    listSize = p.getSupportedPreviewSizes();
/* Log the available video sizes supported and available preview sizes for device.
//                    List<Camera.Size> videosizes = p.getSupportedVideoSizes();
//                    for(int i=0;i<listSize.size();i++)
//                    {
//                        Camera.Size tempSize = listSize.get(i);
//                        Log.v("sizestag", "supported preview size= " + tempSize.width
//                                + ", " + tempSize.height);
//                    }
//                    for(int i=0;i<videosizes.size();i++)
//                    {
//                        Camera.Size tempSize = listSize.get(i);
//                        Log.v("sizestag", "supported video size" + tempSize.width
//                                + ", " + tempSize.height);
//                    }
*/
                    try {
                        Log.d("streamdebug",Util.getProperty("launchRoboFeel",getApplicationContext()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Camera.Size mPreviewSize = listSize.get(2);
                    Log.v(TAG, "preview width = " + mPreviewSize.width
                            + " preview height = " + mPreviewSize.height);
                    p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

                    listSize = p.getSupportedPictureSizes();
                    Camera.Size mPictureSize = listSize.get(2);
                    Log.v(TAG, "capture width = " + mPictureSize.width
                            + " capture height = " + mPictureSize.height);
                    p.setPictureSize(mPictureSize.width, mPictureSize.height);
                    mCamera.setParameters(p);

                    try {
                        mCamera.setPreviewDisplay(holder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCamera.startPreview();
                    mCamera.setDisplayOrientation(90);
//                    mMediaRecorder.setOrientationHint(90);
                    mCamera.unlock();

                    mMediaRecorder = new MediaRecorder();
                    mMediaRecorder.setCamera(mCamera);
                    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

//
                    //amogh add start profile1
                    mMediaRecorder.setVideoEncodingBitRate(100000);
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                    mMediaRecorder.setVideoSize(720,480);
                    mMediaRecorder.setCaptureRate(5);
                    mMediaRecorder.setVideoFrameRate(5);
                    //amogh add finish profile1

                      //default profile
//                    if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
//                    } else {
//                        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
//                    }
                     //default profile ends


                    //amogh add start profile2- need to set frame rate
//                    mMediaRecorder.setVideoSize(720, 480);
//                    mMediaRecorder.setVideoFrameRate(16); //might be auto-determined due to lighting
//                    mMediaRecorder.setVideoEncodingBitRate(3444);
//                    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// MPEG_4_SP
//                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    // amogh add finish profile2


//                    // amogh add start profile3
//                    CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
//                    mMediaRecorder.setOutputFormat(profile.fileFormat);
//                    mMediaRecorder.setVideoEncoder(profile.videoCodec);
////                    mMediaRecorder.setVideoEncodingBitRate(8000000);
//                    mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
////                    mMediaRecorder.setVideoSize(profile.videoFrameWidth,profile.videoFrameHeight);
//                    mMediaRecorder.setVideoSize(352, 288);
//                    mMediaRecorder.setCaptureRate(5);
//                    mMediaRecorder.setVideoFrameRate(5);
//                    // amogh add finish profile3


                    mRecordingPath = Util.getOutputMediaFile(Util.MEDIA_TYPE_VIDEO).getPath();
                    mMediaRecorder.setOutputFile(mRecordingPath);
                    mMediaRecorder.setPreviewDisplay(holder.getSurface());
//                    mMediaRecorder.setVideoEncodingBitRate(3444);
                    try {
                        mMediaRecorder.prepare();
                    } catch (IllegalStateException e) {
                        Log.d(TAG, "IllegalStateException when preparing MediaRecorder: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "IOException when preparing MediaRecorder: " + e.getMessage());
                    }
                    mMediaRecorder.start();

                    resultReceiver.send(RECORD_RESULT_OK, null);
                    Log.d(TAG, "Recording is started");
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                }
            });


            wm.addView(sv, params);

        } else {
            Log.d(TAG, "Get Camera from service failed");
            resultReceiver.send(RECORD_RESULT_GET_CAMERA_FAILED, null);
        }
    }

    private void handleStopRecordingCommand(Intent intent) {
        ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);

        if (!mRecording) {
            // have not recorded
            resultReceiver.send(RECORD_RESULT_NOT_RECORDING, null);
            return;
        }

        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
        } catch (RuntimeException e) {
            mMediaRecorder.reset();
            resultReceiver.send(RECORD_RESULT_UNSTOPPABLE, new Bundle());
            return;
        } finally {
            mMediaRecorder = null;
            mCamera.stopPreview();
            mCamera.release();

            mRecording = false;
        }

        Bundle b = new Bundle();
        b.putString(VIDEO_PATH, mRecordingPath);
        resultReceiver.send(RECORD_RESULT_OK, b);

        Log.d(TAG, "recording is finished.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
