package com.cw.hyplayer.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;


/**
 * @anchor: andy
 * @date: 2018-11-02
 * @description:
 */
public class CameraV1 implements Camera.PreviewCallback {

    private static final String TAG = "CameraV1";

    private SurfaceView mSurfaceView;
    private Context mContext;

    /**
     * 相机实例
     */
    private Camera mCamera;

    /**
     * 默认使用前置摄像头
     */
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public void setContext(Context context) {
        this.mContext = context;
    }

    //    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = openCamera();
    }

    //    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            setCameraDisplayOrientation(mCameraId, mCamera);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFormat(ImageFormat.NV21);
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 相机预览数据输出
     *
     * @param data
     * @param camera
     */
    Camera.Size mPreviewSize;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        this.mPreviewSize = camera.getParameters().getPreviewSize();
        Log.d(TAG, "onPreviewFrame callback");
    }

    public void onDestroy() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera openCamera() {
        Camera camera;
        try {
            camera = Camera.open(mCameraId);
            Log.d(TAG, "openCamera success");
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            camera = null;
        }
        return camera;
    }

    /**
     * 前置摄像头要转换270度
     * 后置摄像头转换90度
     *
     * @param cameraId
     * @param camera
     */
    private void setCameraDisplayOrientation(int cameraId, Camera camera) {
        Activity targetActivity = (Activity) mContext;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = targetActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int displayDegree;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360;
            displayDegree = (360 - displayDegree) % 360;
        } else {
            displayDegree = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(displayDegree);
    }

}
