package com.odogwudev.opengl

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import kotlin.math.abs

class GLRenderer(private val context: Context) : GLSurfaceView.Renderer, SensorEventListener {
    private val vPMatrix = FloatArray(16)
    private lateinit var mCircle: Circle;
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private var ratio = 0f
    private var rotationMatrix = FloatArray(16)
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var rotation: Float = 0f
    private var currentVelocity = 1
    private var velocity = 1
    private var direction = 1
    private var timestamp: Float = 0f

    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        val sensorAcc: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL)

        GLES20.glClearColor(0.5f, 1.0f, 0.5f, 1.0f)
        mCircle = Circle()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(unused: GL10) {
        val scratch = FloatArray(16)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)


        if (mCircle.getModelMatrix()[12] >= ratio * 0.9) {
            if (direction == 1) {
                velocity /= 2
            } else {
                velocity = 1
            }
        } else if (mCircle.getModelMatrix()[12] <= -ratio * 0.9) {
            if (direction == -1) {
                velocity /= 2
            } else {
                velocity = 1
            }
        }

        Matrix.translateM(
            mCircle.getModelMatrix(),
            0,
            direction * (abs(rotation) / 150) * velocity,
            0f,
            0f
        )
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, mCircle.getModelMatrix(), 0)
        mCircle.draw(scratch)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("Sensor", "onSensorChange")

        if (timestamp != 0f && event != null) {
            rotationMatrix = event.values
            rotation = rotationMatrix[1]
            if (rotation < 0) {
                direction = 1
            } else if (rotation > 0) {
                direction = -1
            }
            Log.d("xAxis: ", (rotationMatrix[1]).toString())
        }

        timestamp = event?.timestamp?.toFloat() ?: 0f
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    companion object {
        fun loadShader(type: Int, shaderCode: String): Int {
            return GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }
    }
}