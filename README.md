# ServiceCamera

This is effectively RoboFeel part which records image from camera.

Built on Camera 1 API (Now deprecated)

Built using this app: https://github.com/chartsai/ServiceCamera 

Common problems faced while building:
- Set the buildtool, gradle version according to sdk available on your computer
- Linking Android libraries with [Google Maven repository](https://developer.android.com/studio/build/dependencies#google-maven) (Android Studio prompts automatically though.)

Problems while running:
- Camera might record and save something which does not play - Its important in Camera 1 API for camera parameters etc be saved appropriately. The size framerate etc in this app have been set manually to be able to change the resolution and size according to requirements. You can change that by uncommenting/commenting lines (see the different profiles in CameraService.java).

Works on Pixel and Samsung both.

Basic idea - Call a function startToStartRecording in the CameraService class(which extends Service class) from the main activity which starts the service. What happens when the service starts (initialising camera and setting the preview on surfaceview) is in the function onStartCommand which is overridden. The surface holder's size is (1,1), you can try to increase it to 300,300.

The recorded video will store in /sdcard/Pictures/MyCameraApp/*.mp4

- Support Start/Stop button.
- Support choosing front or back camera for recording.
