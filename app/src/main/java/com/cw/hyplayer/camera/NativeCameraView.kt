package com.cw.hyplayer.camera

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.File

class NativeCameraView : SurfaceView, SurfaceHolder.Callback,
    ICameraSizeListener {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        holder.addCallback(this)
        cameraV2 = CameraV2(context as Activity, this)
        cameraV2?.setCameraSizeListener(this)
    }

    private var active = false
    private var surfaceTexture: SurfaceTexture? = null
    private var cameraV2: CameraV2? = null

    override fun surfaceCreated(holder: SurfaceHolder?) {
        initVideoFile()
        active = true
        nativeCameraCreated(holder!!.surface, this)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        nativeCameraChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        active = false
        cameraV2?.stopPreview()
        cameraV2?.releaseCamera()
        nativeCameraDestroyed()
        surfaceTexture?.release()
    }

    override fun onCameraSizeChanged(width: Int, height: Int) {
        Log.d(TAG, "$width, $height")
        nativeCameraSetSize(width, height)
    }

    fun createOESSurface(oesTextureId: Int) {
        Log.d(TAG, "$oesTextureId")
        surfaceTexture = SurfaceTexture(oesTextureId)
        cameraV2?.setSurfaceTexture(surfaceTexture)
        cameraV2?.openCamera(1080, 1920, 1)
        surfaceTexture?.setOnFrameAvailableListener {
            if (active) {
                nativeCameraDoFrame()
            }
        }
    }

    fun update() {
        surfaceTexture?.updateTexImage()
    }

    private fun initVideoFile() {
        var outputFile =
            File(Environment.getExternalStorageDirectory().absolutePath, "trailer_test.mp4")
        if (outputFile.exists()) {
            outputFile.delete()
            outputFile =
                File(Environment.getExternalStorageDirectory().absolutePath, "trailer_test.mp4")
        }
    }

    fun cameraFFEncodeStart(width: Int, height: Int) {
        nativeCameraFFEncodeStart(width, height)
    }

    fun cameraFFEncodeFrame(data: ByteArray, width: Int, height: Int) {
        nativeCameraFFEncodeFrame(data, width, height)
    }

    fun cameraFFEncodeEnd() {
        nativeCameraFFEncodeEnd()
    }

    private external fun nativeCameraCreated(surface: Surface, nativeCameraView: NativeCameraView)
    private external fun nativeCameraChanged(width: Int, height: Int)
    private external fun nativeCameraDestroyed()
    private external fun nativeCameraDoFrame()
    private external fun nativeCameraSetSize(width: Int, height: Int)
    private external fun nativeCameraFFEncodeStart(width: Int, height: Int)
    private external fun nativeCameraFFEncodeFrame(data: ByteArray, width: Int, height: Int)
    private external fun nativeCameraFFEncodeEnd()

    companion object {
        init {
            System.loadLibrary("native-lib")
        }

        private const val TAG = "HYPlayer"
    }

}