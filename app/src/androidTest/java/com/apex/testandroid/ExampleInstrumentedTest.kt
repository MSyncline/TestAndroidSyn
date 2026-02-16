package com.apex.testandroid

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    companion object {
        private const val TAG = "ExampleInstrumentedTest"
    }

    @Test
    fun useAppContext() {
        Log.d(TAG, "Starting useAppContext test")
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Log.d(TAG, "Package name: ${appContext.packageName}")
        assertEquals("com.apex.testandroid", appContext.packageName)
        Log.d(TAG, "Test passed successfully")
    }
}