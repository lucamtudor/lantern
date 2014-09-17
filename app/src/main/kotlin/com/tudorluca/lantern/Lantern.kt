package com.tudorluca.lantern

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.Parameters
import android.widget.Toast

import java.io.IOException

public class Lantern(val context: Context) {

    private var mCamera: Camera?
    private var mDummySurfaceTexture: SurfaceTexture? = null
    private var mIsOn = false

    {
        mCamera = Camera.open()
    }

    public fun isOn(): Boolean {
        return mIsOn
    }

    public fun hasLantern(): Boolean {
        return isFlashModeTorchSupported(mCamera)
    }

    SuppressLint("NewApi")
    public fun turnOn() {
        if (mCamera == null) {
            // The devices has no camera. A white screen is the best option.
            Toast.makeText(context, "No camera.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isFlashModeTorchSupported(mCamera)) {
            Toast.makeText(context, "No flash.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (mDummySurfaceTexture == null) {
                mDummySurfaceTexture = SurfaceTexture(0)
            }
            mCamera!!.setPreviewTexture(mDummySurfaceTexture)
            mCamera!!.startPreview()

            val params = mCamera!!.getParameters()
            params.setFlashMode(Parameters.FLASH_MODE_TORCH)
            mCamera!!.setParameters(params)

            mIsOn = true
        } catch (e: IOException) {
            throw RuntimeException("Can't open flash!", e)
        }


    }

    public fun turnOff() {
        if (!hasLantern()) {
            Toast.makeText(context, "No camera", Toast.LENGTH_SHORT).show()
            return
        }

        val params = mCamera!!.getParameters()
        params.setFlashMode(Parameters.FLASH_MODE_OFF)
        mCamera!!.setParameters(params)
        mCamera!!.stopPreview()

        mIsOn = false
    }

    public fun set(on: Boolean) {
        if (on) {
            turnOn()
        } else {
            turnOff()
        }
    }

    SuppressLint("NewApi")
    public fun release() {
        mCamera?.release()
        mDummySurfaceTexture?.release()
        mCamera = null
        mDummySurfaceTexture = null
    }

    private fun isFlashModeTorchSupported(camera: Camera?): Boolean {
        if (camera != null) {
            val params = camera.getParameters()
            val supportedFlashModes = params.getSupportedFlashModes()
            return supportedFlashModes?.contains(Parameters.FLASH_MODE_TORCH) ?: false
        }
        return false
    }
}
