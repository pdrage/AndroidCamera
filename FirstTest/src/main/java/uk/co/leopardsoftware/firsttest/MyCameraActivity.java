package uk.co.leopardsoftware.firsttest;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

//import org.apache.commons.net.ftp.*;


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


      Spinner spinner = (Spinner) findViewById(R.id.docType_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
      ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
              R.array.docTypes, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
      spinner.setAdapter(adapter);


    task.execute(new String[] { "" });
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
           case Surface.ROTATION_0: degrees = 90; break;
           case Surface.ROTATION_90: degrees = 180; break;
           case Surface.ROTATION_180: degrees = 270; break;
           case Surface.ROTATION_270: degrees = 0; break;
        }
 //       degrees=90;

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

    ImageView captureButton = (ImageView) findViewById(R.id.capture);
    captureButton.setOnClickListener(
       new View.OnClickListener() {
       public void onClick(View v) {
        getImage();
      }
      }
    );
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

                  MySFTPClient mFTPClient = null;
                  boolean status = false;

                File directory = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), getPackageName());
                //creates this directory if its not there??
                directory.mkdirs();
                File sd = new File(directory.getAbsolutePath());

                int numfiles =0;
                File[] sdDirList = sd.listFiles();
                if (sdDirList != null) {
                    Log.d(TAG, "There are " + sdDirList.length + " files in  " + sd.toString());
                    numfiles = sdDirList.length;
                }
                if (numfiles > 0 ) {

                    mFTPClient = new MySFTPClient();
                      // connecting to the host
                    status = mFTPClient.ftpConnect("www.compli-staging.depoel.local", "pdrage", "Dr@g3-2014",22);
                      // now check the reply code, if positive mean connection success
                      Log.i(TAG, "FTPconnect has status of " + status);

                      if (status) {
                /* debug - show Pictures Directory */
                        Log.d(TAG, "Directory = " + directory.getAbsolutePath());
                /* debug get REMOTE working directory */
                        String WD = mFTPClient.ftpGetCurrentWorkingDirectory();
                        Log.d(TAG, "Working directory is " + WD);

                  /* now send a file */
                          String desDirectory = "public_html/CWU";
                          status = mFTPClient.ftpChangeDirectory(desDirectory);

                      //gets a list of the files
                          try{


                            for(int i=sdDirList.length-1;i>-1;i--){

                              String srcFilePath = sdDirList[i].toString();
                              String desFileName = sdDirList[i].toString().substring(sdDirList[i].toString().lastIndexOf("/")+1);
                              Log.d (TAG, "uploading " +srcFilePath + " as " + desFileName + " into " + desDirectory);
                              status = mFTPClient.ftpUpload( srcFilePath, desFileName, desDirectory, getApplicationContext());
                              Log.d(TAG, "FTP upload request has status of " + status);
                              // delete the source file..
                              if(status) sdDirList[i].delete();
                            } // end for
                          } // end try
                          catch (Exception e){
                            Log.e(TAG, "Exception listing directory " + e.getMessage());
                          } // end catch
                      } // end if status OK on connect
                    Log.i(TAG, "Disconnect");
                    mFTPClient.ftpDisconnect();
                } // end if files to transmit
                Log.d(TAG, "Sleep");

//                Thread.sleep(10000); //  5 or 100 seconds
                Thread.sleep(100000); //  5 or 100 seconds
//                Log.e(TAG, "Change Sleep period to 100 seconds. Set to 10 seconds for testing");
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
        Log.d(TAG, "File written to store");
        return null;
     }
  }
}
