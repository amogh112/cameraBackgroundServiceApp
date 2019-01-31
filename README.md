# ServiceCamera

This is effectively RoboFeel part which records image from camera.

Built on Camera 1 API (Now deprecated)

Built using this app: https://github.com/chartsai/ServiceCamera 

Common problems faced while building:
- Set the buildtool, gradle version according to sdk available on your computer
- Linking Maven(Android Studio prompts automatically though.)

Problems while running:
- Camera might record and save something which does not play - Its important in Camera 1 API for camera parameters etc be saved appropriately. The size framerate etc in this app have been set manually to be able to change the resolution and size according to requirements. You can change that by uncommenting/commenting lines.

The recorded video will store in /sdcard/Pictures/MyCameraApp/*.mp4

- Support Start/Stop button.
- Support choosing front or back camera for recording.
