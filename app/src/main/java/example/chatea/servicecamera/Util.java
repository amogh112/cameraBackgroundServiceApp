package example.chatea.servicecamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by charliet on 3/19/15.
 */
public class Util {

    public static boolean isCameraExist(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCameraExist(int cameraId) {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == cameraId) {
                return true;
            }
        }
        return false;
    }

    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId);
        } catch (Exception e) {
            Log.d("TAG", "Open camera failed: " + e);
        }

        return c;
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
            try {
                mediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }

        return mediaFile;
    }
    public static String getProperty(String key, Context context) throws IOException {
        Properties properties = new Properties();

        AssetManager assetManager = context.getAssets();

        //seeing the files
//        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath();
//        Log.d("Files", "Path: " + path2);
//        File f = new File(path2);
//        File file[] = f.listFiles();
//        Log.d("Files", "Size: "+ file.length);
////        for (int i=0; i < file.length; i++)
////        {
////            Log.d("Files", "FileName:" + file[i].getName());
////        }

        String fileName = "config.properties";
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName;
        Log.d("streamdebug2", path);


        File file = new File(path);
//        InputStream inputStream = context.getClass().getClassLoader().getResourceAsStream("example.properties");
        InputStream inputStream = new FileInputStream(path);
//         InputStream inputStream = context.openFileInput(path);

//        InputStream inputStream = assetManager.open("config.properties");

//        properties.load(new FileInputStream(path));
        properties.load(inputStream);
        inputStream.close();
        return properties.getProperty(key);
    }
}
