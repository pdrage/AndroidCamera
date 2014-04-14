package uk.co.leopardsoftware.firsttest;

import java.io.IOException;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.os.Build;

public class CameraPreview extends SurfaceView 
                           implements SurfaceHolder.Callback {
  private static final String TAG = "CameraPreview";
  private SurfaceHolder sh;
  private Camera camera;
  private boolean isPreviewRunning;
  
  public CameraPreview(Context context, Camera cm) {
    super(context);
      Log.e(TAG, "Starting Preview");
    camera = cm;
      Log.e(TAG, "camera passed in from call - called camera");
      // orientation fix
    //    // orientation fix
    sh = getHolder();
    sh.addCallback(this);
    // deprecated but required pre-3.0
    sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    isPreviewRunning = true;
  }


    public void surfaceCreated(SurfaceHolder holder)
    {
        try {
//        camera = Camera.open();

            Parameters params = camera.getParameters();
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
            {
                Log.d(TAG, "options being set");
                params.set("orientation", "portrait");
                camera.setDisplayOrientation(90);
                camera.setParameters(params);
            } else {
                Log.d(TAG, "options NOT being set");

            }


            camera.setPreviewDisplay(holder);
        }
        catch (IOException exception)
        {
            camera.release();
            camera = null;
        }
//
//        try {
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
//            {
//                Log.d(TAG, "option 1");
//                camera.setDisplayOrientation(90);
//            } else {
//                Log.d(TAG, "option 2");
//
//                parameters.setRotation(90);
//                camera.setParameters(parameters);
//            }
//
//            camera.setPreviewDisplay(holder);
//        } catch (IOException exception) {
//            camera.release();
//        }
//
//        camera.startPreview();
//    } catch (Exception e) {
//          Log.e(TAG, "Error setting up preview: " + e.getMessage());
//          e.getStackTrace();
//    }
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {
    if (sh.getSurface() == null) {
      // no preview surface!
      return;
    }
    
    // Stop preview before changing.

        if (isPreviewRunning) {
            camera.stopPreview();
        }

        Parameters parameters = camera.getParameters();
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        if (display.getRotation() == Surface.ROTATION_0) {
            parameters.setPreviewSize(height, width);
            camera.setDisplayOrientation(90);
        }

        if (display.getRotation() == Surface.ROTATION_90) {
            parameters.setPreviewSize(width, height);
        }

        if (display.getRotation() == Surface.ROTATION_180) {
            parameters.setPreviewSize(height, width);
        }

        if (display.getRotation() == Surface.ROTATION_270) {
            parameters.setPreviewSize(width, height);
            camera.setDisplayOrientation(180);
        }

        camera.setParameters(parameters);
        camera.startPreview();

  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    // Activity looks after releasing camera preview
  }

}
