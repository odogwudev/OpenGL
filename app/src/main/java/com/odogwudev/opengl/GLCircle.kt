package com.odogwudev.opengl

import com.odogwudev.opengl.GLRenderer.Companion.loadShader
import android.content.res.Resources
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val COORDS_PER_VERTEX = 3
var circleCoords = floatArrayOf(     // in counterclockwise order:
    0.0f, 0.622008459f, 0.0f,        // top
    -0.5f, -0.311004243f, 0.0f,      // bottom left
    0.5f, -0.311004243f, 0.0f        // bottom right
)

class Circle {
    public val mModelMatrix = FloatArray(16)
    val color = floatArrayOf(0f, 0f, 1f, 1.0f)
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var mMVPMatrixHandle = 0
    private val mVertexBuffer: FloatBuffer
    private val vertexCount: Int = circleCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4
    private val vertices = FloatArray(364 * 3)
    private var mProgram: Int

    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                " gl_Position = uMVPMatrix * vPosition;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"


    init {
        Matrix.setIdentityM(mModelMatrix, 0)
        vertices[0] = 0F
        vertices[1] = 0F
        vertices[2] = 0F
        val ratio =
            Resources.getSystem().displayMetrics.widthPixels.toFloat() / Resources.getSystem().displayMetrics.heightPixels.toFloat()
        for (i in 1..363) {
            vertices[i * 3 + 0] = (ratio * 0.1 * cos(3.14 / 180 * i.toFloat())).toFloat()
            vertices[i * 3 + 1] = (ratio * 0.1 * sin(3.14 / 180 * i.toFloat())).toFloat()
            vertices[i * 3 + 2] = 0F
        }
        val vertexByteBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        vertexByteBuffer.order(ByteOrder.nativeOrder())
        mVertexBuffer = vertexByteBuffer.asFloatBuffer()
        mVertexBuffer.put(vertices)
        mVertexBuffer.position(0)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)
        GLES20.glLinkProgram(mProgram)
    }

    fun getModelMatrix(): FloatArray {
        return mModelMatrix;
    }

    fun draw(mvpMatrix: FloatArray?) {
        GLES20.glUseProgram(mProgram)
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)

            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                mVertexBuffer
            )

            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
            GLES20.glUniform4fv(mColorHandle, 1, color, 0)

            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 364)
            GLES20.glDisableVertexAttribArray(it)
        }
    }


}