package com.donnemartin.android.notes.notes;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.Button;

import java.io.IOException;
import java.util.List;

public class NoteCameraFragment extends Fragment {

    private static final String TAG = "NoteCameraFragment";

    private Camera mCamera;
    private SurfaceView mSurfaceview;

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
                getActivity().finish();
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

        return view;
    }

    @TargetApi(9)
    @Override
    public void onResume() {
        super.onResume();

        // XXX: Opening camera on the main thread, use multi-threading instead
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
