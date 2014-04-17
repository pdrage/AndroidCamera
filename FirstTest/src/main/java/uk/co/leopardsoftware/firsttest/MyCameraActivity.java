package uk.co.leopardsoftware.firsttest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.Toast;

//import org.apache.commons.net.ftp.*;


public class MyCameraActivity extends Activity {
  
  // Use constants above API 11 (MediaStore.Files.FileColumns)
  protected static final int MEDIA_TYPE_IMAGE = 0;
  private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 100;
  private static final String TAG = "MCAct";

  private Camera camera;
  private CameraPreview preview;
  private MediaRecorder mr;
  public FTPClass task = null;
  private KeyValueSpinner DocTypeAdapter;
  private String state = "IDLE";

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!checkCameraExists(this)) {
      Toast.makeText(this, "Sorry: you have no camera!", Toast.LENGTH_LONG).show();
      finish();
    }
    camera = getCameraInstance();
      setCameraDisplayOrientation(this, 0, camera);
    setUpLayout();

     FTPClass task = new FTPClass();


      Spinner spin = (Spinner) findViewById(R.id.docType_spinner);
      ArrayList alDocType = new ArrayList();
      String docTypesPath = Environment.getExternalStoragePublicDirectory(
              "uk.co.leopardsoftware") + "/DocumentTypes.csv";
      Log.e(TAG, "Path is " + docTypesPath);

      try {
          String[] docArray = OpenFile(docTypesPath);
          for (int i = 0; i < docArray.length; i++) {
              String[] parts = docArray[i].split(",", 2);
              alDocType.add(new DocumentType(Integer.parseInt(parts[0]), parts[1]));
          }
      } catch (IOException e){
          Log.e(TAG, "Failed to open Document Type input file");
      }

      DocTypeAdapter = new KeyValueSpinner(this, alDocType);
      spin.setAdapter(DocTypeAdapter);
      EditText CWUbox = (EditText)findViewById(R.id.CWU_id);
      CWUbox.setSelection(CWUbox.getText().length());

// hide the status and navigation bars ..
      View decorView = getWindow().getDecorView();
// Hide the status bar.
      int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
      decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.
      ActionBar actionBar = getActionBar();
      actionBar.hide();


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
      } else if (!(resultCode == RESULT_CANCELED)) {
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
    // hide the status and navigation bars ..
    View decorView = getWindow().getDecorView();
    // Hide the status bar.
    int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
    decorView.setSystemUiVisibility(uiOptions);
    // Remember that you should never show the action bar if the
    // status bar is hidden, so hide that too if necessary.
    ActionBar actionBar = getActionBar();
    actionBar.hide();
    super.onResume();
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
      Toast.makeText(this, "Sorry: I can't get a camera!", Toast.LENGTH_LONG).show();
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

//      new SaveImageTask().execute(data);
        Log.d(TAG, "return from SaveImageTask");
// - wait until accepted or rejected..
//        camera.startPreview();
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
    String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
    File file;
    Log.e(TAG, "Parsing data to create filename");

 /*
 *  File name formats :
 *  CWUnnnnnnn-xx-mm-ii-YYYYMMDD_bbbbbb.jpg
 * or maybe...
 *  YYYYMMDD-CWUnnnnnnn-xx-mm-bbbbbb-ii-.jpg
 *
 *   where
 *           xx is the id of the tablet that captured the image.
 *       nnnnnn is the internal CWU reference for the candidate.
 *           mmm is the CWU id of the document type.
 *            i is zero (00) if this is a single document or 01-99 if this is part of a sequence.
 *     YYYYMMDD is the date of the file (from the App).
 *       bbbbbb is the sequential number assigned to the image by the App.
 */
    String filename;
    String tabletId = "01";
    String seq = "00";
    String CWU_id ="0";

    EditText mEdit   = (EditText)findViewById(R.id.CWU_id);
    try{
        CWU_id = String.format("%06d", Integer.parseInt(mEdit.getText().toString()));
    } catch (Exception e){
        Log.e(TAG, "Formatting exception \n "+ e.getMessage());
    }

    String fileDate = "YYYYMMDD";
    try {
          Date now = new Date();
          SimpleDateFormat format =
                  new SimpleDateFormat("yyyyMMdd");
      //    Date parsed = format.parse(dateString);
          fileDate = format.format(now);
    } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "Failed to format date");

    }

    Spinner dc = (Spinner)findViewById(R.id.docType_spinner);
    Integer pos = dc.getSelectedItemPosition();
    Integer doctype = DocTypeAdapter.getIDFromIndex(pos);
    String  doctype_s = String.format("%03d", doctype);

    filename = fileDate + "-CWU" + CWU_id + "-"+ tabletId + "-" +doctype_s + "-";
    filename = filename + timeStamp + "-" + seq;

    Log.e(TAG, "DocumentType =  " + doctype);
    Log.e(TAG, "Filename = " + filename);

    file = new File(directory.getPath() + File.separator + filename + ".jpg");
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
            state = "HOLDING";
        }
      }
    );

      final Context myContext = this;
    ImageView acceptButton = (ImageView) findViewById(R.id.accept);
    acceptButton.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(myContext, "Click on accept", Toast.LENGTH_LONG).show();
                    state="IDLE";
                    camera.startPreview();
                }
           }
    );

      ImageView cancelButton = (ImageView) findViewById(R.id.cancel);
      cancelButton.setOnClickListener(
              new View.OnClickListener() {
                  public void onClick(View v) {
                      Toast.makeText(myContext, "Click on cancel", Toast.LENGTH_LONG).show();
                      state="IDLE";
                      camera.startPreview();

                  }
              }
      );

      ImageView zoomButton = (ImageView) findViewById(R.id.zoom);
      zoomButton.setOnClickListener(
              new View.OnClickListener() {
                  public void onClick(View v) {
                      Toast.makeText(myContext, "Click on zoom", Toast.LENGTH_LONG).show();
                  }
              }
      );


  }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        task.cancel(true);
    }

    protected  String[] OpenFile(String path) throws  IOException {
        FileReader fr  = new FileReader(path);
        BufferedReader textReader = new BufferedReader(fr);

        int numberOfLines =readLines(path);
        String[] textData = new String[numberOfLines];

        int i;

        for (i=0; i< numberOfLines; i++){
            textData[i] = textReader.readLine();
        }
        textReader.close( );
        return textData;
    }

    protected int readLines(String path) throws  IOException {
        FileReader file_to_read = new FileReader(path);
        BufferedReader bf =new BufferedReader(file_to_read);

        int numberOfLines =0;
        while ((bf.readLine()) != null) {
            numberOfLines++;
        }
        bf.close();
        return numberOfLines;
    }

    /*
     *
     **************************  FTP Code ************
     *
     *
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

                  MySFTPClient mFTPClient;
                  boolean status;

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
                          mFTPClient.ftpChangeDirectory(desDirectory);

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
