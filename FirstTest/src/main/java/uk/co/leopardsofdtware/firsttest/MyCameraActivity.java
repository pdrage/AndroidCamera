package uk.co.leopardsofdtware.firsttest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.os.Build;

import org.apache.commons.net.ftp.*;


public class MyCameraActivity extends Activity {
  
  // Use constants above API 11 (MediaStore.Files.FileColumns)
  protected static final int MEDIA_TYPE_IMAGE = 0;
  protected static final int MEDIA_TYPE_VIDEO = 1;
  private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 100;
  private static final String TAG = "MCAct";

  private Uri fileUri;
  private Camera camera;
  private CameraPreview preview;
  private MediaRecorder mr;
  private Button videoButton;
  protected boolean isRecording = false;
  public FTPClass task = null;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!checkCameraExists(this)) {
      Toast.makeText(this, "Sorry: you have no camera!", Toast.LENGTH_LONG);
      finish();
    }
    camera = getCameraInstance();
      setCameraDisplayOrientation(this, 0, camera);
    setUpLayout();

     FTPClass task = new FTPClass();
    task.execute(new String[] { "http://www.vogella.com" });
  }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        Log.e(TAG, "Rotation is " + rotation);
        int degrees = 0;
        switch (rotation) {
 //           case Surface.ROTATION_0: degrees = 0; break;
 //           case Surface.ROTATION_90: degrees = 90; break;
 //           case Surface.ROTATION_180: degrees = 180; break;
 //           case Surface.ROTATION_270: degrees = 270; break;
        }
        degrees=90;

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    // Method required if setting up an Intent button
  // to call the built-in camera
  protected void onActivityResult(int requestCode, int resultCode, 
                                  Intent data) {
    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
      if (resultCode == RESULT_OK) {
        if (data == null) {
          // A known bug here! The image should have saved in fileUri
          Toast.makeText(this, "Image saved successfully", 
                         Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(this, "Image saved successfully in: " 
                         + data.getData(), Toast.LENGTH_LONG).show();
        }
      } else if (resultCode == RESULT_CANCELED) {
        // User cancelled the operation; do nothing
      } else {
        Toast.makeText(this, "Callout for image capture failed!", 
                       Toast.LENGTH_LONG).show();
      }
    }
  }

  protected void onPause() {
    releaseMediaRecorder();
    releaseCamera();
    super.onPause();
  }

  protected void onResume() {
    if (camera == null) {
      camera = getCameraInstance();
      setUpLayout();
    }
    super.onResume();
  }

  protected Uri getOutputMediaFileUri(int type) {
    return Uri.fromFile(getOutputMediaFile(type));
  }

  
  protected boolean prepareForVideoRecording() {
    camera.unlock();
    mr = new MediaRecorder();
    mr.setCamera(camera);
    mr.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
    mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    mr.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
    mr.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
    mr.setPreviewDisplay(preview.getHolder().getSurface());
    try {
      mr.prepare();
    } catch (IllegalStateException e) {
      Log.e(TAG, "IllegalStateException when preparing MediaRecorder " 
            + e.getMessage());
      e.getStackTrace();
      releaseMediaRecorder();
      return false;
    } catch (IOException e) {
      Log.e(TAG, "IllegalStateException when preparing MediaRecorder " 
            + e.getMessage());
      e.getStackTrace();
      releaseMediaRecorder();
      return false;
    }
    return true;
  }

  private boolean checkCameraExists(Context c) {
    if (c.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      return true;
    } else {
      return false;
    }
  }

  private Camera getCameraInstance() {
    Camera c = null;
    try {
      c = Camera.open();
    } catch (Exception e) {
      Log.e(TAG, "No camera: exception " + e.getMessage());
      e.getStackTrace();
      Toast.makeText(this, "Sorry: I can't get a camera!", Toast.LENGTH_LONG);
      finish();
    }
    return c;
  }

  private void getImage() {
    PictureCallback picture = new PictureCallback() {
      public void onPictureTaken(byte[] data, Camera cam) {
          Log.d(TAG, "onPictureTaken has been called");

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
              new SaveImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
          else
              new SaveImageTask().execute(data);

//        new SaveImageTask().execute(data);
          Log.d(TAG, "return from SaveImageTask");
        camera.startPreview();
          Log.d(TAG, "preview restarted");
      }
    };
    Log.e(TAG,"Calling TakePicture");
    camera.takePicture(null, null, picture);
     Log.e(TAG,"Returned from  TakePicture");
  }

  private File getOutputMediaFile(int type) {
    // good location for shared pictures; will not be lost if app uninstalled
    File directory = new File(Environment.getExternalStoragePublicDirectory(
      Environment.DIRECTORY_PICTURES), getPackageName());
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        Log.e(TAG, "Failed to create storage directory.");
        return null;
      }
      Log.e(TAG,"Made directory");
    } else {
        Log.e(TAG, "Directory exists, about to write file");
    }
    String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss")
                       .format(new Date());
    File file;
    if (type == MEDIA_TYPE_IMAGE) {
      file = new File(directory.getPath() + File.separator + "IMG_" 
                    + timeStamp + ".jpg");
    } else if (type == MEDIA_TYPE_VIDEO) {
      file = new File(directory.getPath() + File.separator + "VID_" 
                      + timeStamp + ".mp4");
    } else {
      return null;
    }
    return file;
  }

  private void releaseCamera() {
    if (camera != null) {
      camera.stopPreview();
      camera.release();
      camera = null;
      preview = null;
    }
  }
  
  private void releaseMediaRecorder() {
    if (mr != null) {
      mr.reset();
      mr.release();
      mr = null;
      camera.lock();
    }
  }

  private void setUpLayout() {
    setContentView(R.layout.activity_main);
      Log.e(TAG, "in SetupLayout");
    preview = new CameraPreview(this, camera);
    FrameLayout frame = (FrameLayout) findViewById(R.id.camera_preview);
    frame.addView(preview);

    Button captureButton = (Button) findViewById(R.id.button_capture);
    captureButton.setOnClickListener(
       new View.OnClickListener() {
       public void onClick(View v) {
        getImage();
      }
      }
    );
    setUpFlashButton();
    setUpIntentButton();
    setUpVideoButton();
  }

  private void setUpFlashButton() {
    final Camera.Parameters params = camera.getParameters();
    final List<String> flashList = params.getSupportedFlashModes();
    if (flashList == null) {
      // no flash!
      return;
    }
    final CharSequence[] flashCS = flashList.toArray(
                                   new CharSequence[flashList.size()]);
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Choose flash type");
    builder.setSingleChoiceItems(flashCS, -1, 
                                 new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        params.setFlashMode(flashList.get(which));
        camera.setParameters(params);
        Toast.makeText(getApplicationContext(), params.getFlashMode(), 
                       Toast.LENGTH_SHORT).show();
        dialog.dismiss();
      }
    });
    final AlertDialog alert = builder.create();

    Button flashButton = new Button(this);
    setUpButton(flashButton, "flash");
    flashButton.setOnClickListener(
      new View.OnClickListener() {
         public void onClick(View v) {
          alert.show();
        }
      }
    );
  }

  private void setUpIntentButton() {
    Button intentButton = new Button(this);
    setUpButton(intentButton, "Open built-in app");
    intentButton.setOnClickListener(
      new View.OnClickListener() {
        public void onClick(View v) {
          Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
          fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
          Log.v(TAG, "fileUri: " + fileUri);
          i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
          startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ);
      }
      }
    );  
  }

  private void setUpVideoButton() {
    videoButton = new Button(this);
    setUpButton(videoButton, "Start video");

    videoButton.setOnClickListener(
      new View.OnClickListener() {
        public void onClick(View v) {
          if (isRecording) {
            mr.stop();
            releaseMediaRecorder();
            camera.lock();
            videoButton.setText("Start video");
            isRecording = false;
          } else {
            if (prepareForVideoRecording()) {
              mr.start();
              videoButton.setText("Stop video");
              isRecording = true;
            } else {
              // Something has gone wrong! Release the camera
              releaseMediaRecorder();
              Toast.makeText(MyCameraActivity.this, 
                             "Sorry: couldn't start video", 
                             Toast.LENGTH_LONG).show();
            }
          }
        }
      }
    );  
  }
  
  private void setUpButton(Button button, String label) {
    LinearLayout lin = (LinearLayout) findViewById(R.id.buttonlayout);
    button.setText(label);
    button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 
                 LayoutParams.WRAP_CONTENT));
    lin.addView(button);    
  }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        task.cancel(true);
    }

    /*
    **************************  FTP Code ************
     */

  private class FTPClass  extends AsyncTask<String, Void, String>{

      private boolean running = true;

      @Override
      protected void onCancelled() {
          running = false;
      }

      @Override
      protected String doInBackground(String... urls) {

        Log.e (TAG, "Urls = " + urls);
        while(running) {
            try {
    /* Loop forever */


    /* Connect to an FTP server */

                  MyFTPClient mFTPClient = null;
                  boolean status = false;

                File directory = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), getPackageName());
                //creates this directory if its not there??
                directory.mkdirs();
                File sd = new File(directory.getAbsolutePath());
                Log.e(TAG, "Source Directory is " + sd.toString() );

                int numfiles =0;
                File[] sdDirList = sd.listFiles();
                if (sdDirList != null) {
                    Log.e(TAG, "There are " + sdDirList.length + " files in  " + sd.toString());
                    numfiles = sdDirList.length;
                }
                if (numfiles > 0 ) {

                    mFTPClient = new MyFTPClient();
                      // connecting to the host
// Secure                      status = mFTPClient.ftpConnect("ftp.drage.me.uk", "drageuk", "april96",21);
                    status = mFTPClient.ftpConnect("www.compli-staging.depoel.local", "pdrage", "Dr@g3-2014",22);
                      // now check the reply code, if positive mean connection success
                      Log.e(TAG, "FTPconnect has status of " + status);
                      if (status) {

                  /* debug - show Pictures Directory */
                          Log.e(TAG, "Directory = " + directory.getAbsolutePath());

                  /* debug get REMOTE working directory */
                        String WD = mFTPClient.ftpGetCurrentWorkingDirectory();
                        Log.e(TAG, "Working directory is " + WD);

                  /* now send a file */
                          String desDirectory = "/public_html/CWU";
                          status = mFTPClient.ftpChangeDirectory(desDirectory);

                      //gets a list of the files
                          try{


                            for(int i=sdDirList.length-1;i>-1;i--){

                              String srcFilePath = sdDirList[i].toString();
                              String desFileName = sdDirList[i].toString().substring(sdDirList[i].toString().lastIndexOf("/")+1);
                              Log.e (TAG, "uploading " +srcFilePath + " as " + desFileName + " into " + desDirectory);
                              status = mFTPClient.ftpUpload( srcFilePath, desFileName, desDirectory, getApplicationContext());
                              Log.e(TAG, "FTP upload request has status of " + status);
                              // delete the source file..
                              if(status) sdDirList[i].delete();
                            } // end for
                          } // end try
                          catch (Exception e){
                            Log.e(TAG, "Exception listing directory " + e.getMessage());
                          } // end catch
                      } // end if status OK on connect
                    Log.e(TAG, "Disconnect");
                    mFTPClient.ftpDisconnect();
                } // end if files to transmit
                Log.e(TAG, "Sleep");

                Thread.sleep(10000); //  5 or 100 seconds
    //            Thread.sleep(100000); //  5 or 100 seconds
                Log.e(TAG, "Change Sleep period to 100 seconds. Set to 10 seconds for testing");
              } //. end of loop
          catch (Exception e){
            e.printStackTrace();
            }
        } // end of while loop

    return "OK";
  }

    @Override
    protected void onPostExecute(String result) {
        Log.e(TAG, "onPostExecute run");
    }
}

  
  class SaveImageTask extends AsyncTask<byte[], String, String> {
    @Override
    protected String doInBackground(byte[]... data) {
        File picFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (picFile == null) {
        Log.e(TAG, "Error creating media file; are storage permissions correct?");
        return null;
        }
        try {
        Log.d(TAG,"started SaveImageTask");
        FileOutputStream fos = new FileOutputStream(picFile);
         fos.write(data[0]);
         fos.close();
        } catch (FileNotFoundException e) {
         Log.e(TAG, "File not found: " + e.getMessage());
          e.getStackTrace();
        } catch (IOException e) {
         Log.e(TAG, "I/O error with file: " + e.getMessage());
         e.getStackTrace();
        }
        Log.d(TAG, "File written");
        return null;
     }
  }
}
