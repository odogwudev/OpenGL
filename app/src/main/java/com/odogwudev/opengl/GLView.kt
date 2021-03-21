package com.odogwudev.opengl

import android.content.Context
import android.opengl.GLSurfaceView

class GLView(context: Context) : GLSurfaceView(context) {

    private val renderer: GLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = GLRenderer(context)
        setRenderer(renderer)
    }

}
