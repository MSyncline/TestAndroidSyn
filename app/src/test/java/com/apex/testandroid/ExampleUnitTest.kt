package com.apex.testandroid

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        println("ExampleUnitTest: Starting addition_isCorrect test")
        val result = 2 + 2
        println("ExampleUnitTest: 2 + 2 = $result")
        assertEquals(4, result)
        println("ExampleUnitTest: Test passed successfully")
    }
}