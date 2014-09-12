package com.donnemartin.android.notes.notes;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class NoteCameraFragment extends Fragment {

    private static final String TAG = "NoteCameraFragment";
    public static final String EXTRA_PHOTO_FILENAME =
        "com.donnemartin.android.notes.photo_filename";

    private Camera mCamera;
    private SurfaceView mSurfaceview;
    private View mProgressContainer;

    private Camera.ShutterCallback mShutterCallback =
        new Camera.ShutterCallback() {
        public void onShutter() {
            // Display the progress indicator
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };

    private Camera.PictureCallback mJpegCallBack =
        new Camera.PictureCallback() {
        public void onPictureTaken(byte[] dataJpeg, Camera camera) {
            String fileName = UUID.randomUUID().toString() + ".jpg";
            FileOutputStream os = null;
            boolean success = true;

            try {
                os = getActivity().openFileOutput(fileName,
                                                  Context.MODE_PRIVATE);
                os.write(dataJpeg);
            } catch (Exception e) {
                Log.e(TAG, "Error writing to file " + fileName, e);
                success = false;
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error closing file " + fileName, e);
                    success = false;
                }
            }

            if (success) {
                // Set the photo filename on the result intent
                if (success) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_PHOTO_FILENAME, fileName);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                } else {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                }
            }

            getActivity().finish();
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup parent,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_camera,
                                     parent,
                                     false);

        Button takePictureButton = (Button)view
            .findViewById(R.id.note_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (mCamera != null) {
                    mCamera.takePicture(mShutterCallback, null, mJpegCallBack);
                }
            }
        });

        mSurfaceview = (SurfaceView)view
            .findViewById(R.id.note_camera_surfaceView);
        SurfaceHolder holder = mSurfaceview.getHolder();
        // Both setType() and SURFACE_TYPE_PUSH_BUFFERS are deprecated
        // but are both required for Camera preview to work on pre-3.0 devices
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {

            public void surfaceCreated(SurfaceHolder holder) {
                // Tell the camera to use this surface as its preview area
                // Connect the surface with its client the camera
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (IOException exception) {
                    Log.e(TAG, "Error setting up preview display", exception);
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // Called when the SurfaceView is removed from the screen
                // We can no longer display on this surface, so stop the preview
                if (mCamera != null) {
                    // Stop drawing frames on the surface
                    mCamera.stopPreview();
                }
            }

            public void surfaceChanged(SurfaceHolder holder,
                                       int format,
                                       int width,
                                       int height) {
                // Called when the surface is being displayed for the first time
                // Tell the surface's client how big the drawing will be
                if (mCamera != null) {
                    // The surface changed size; update the camera preview size
                    Camera.Parameters params = mCamera.getParameters();
                    Camera.Size size =
                        getOptimalPreviewSize(params.getSupportedPreviewSizes(),
                                              width,
                                              height);
                    params.setPreviewSize(size.width, size.height);

                    size =
                        getOptimalPreviewSize(params.getSupportedPictureSizes(),
                                              width,
                                              height);
                    params.setPictureSize(size.width, size.height);

                    List<String> focusModes = params.getSupportedFocusModes();
                    if (focusModes
                        .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }

                    mCamera.setParameters(params);

                    try {
                        // Start drawing frames on the surface
                        mCamera.startPreview();
                    } catch (Exception e) {
                        Log.e(TAG, "Could not start preview", e);
                        mCamera.release();
                        mCamera = null;
                    }
                }
            }
        });

        mProgressContainer =
            view.findViewById(R.id.note_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);

        return view;
    }

    @TargetApi(9)
    @Override
    public void onResume() {
        super.onResume();

        // TODO: Opening camera on the main thread, use multi-threading instead
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            // Open the first camera, which is usually the rear-facing
            // camera, or the front facing camera if there is no rear
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes,
                                              int w,
                                              int h) {
        // Taken from CameraPreview.java in the ApiDemos Android sample app
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;

        if (sizes == null) {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;

            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }
}
