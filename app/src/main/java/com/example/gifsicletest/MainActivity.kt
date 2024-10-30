package com.example.gifsicletest

import GlassEffectView
import LuckyWheelView
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.gifsicletest.ui.theme.GifsicleTestTheme
import java.io.File
import java.util.Locale

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.random.Random

// 修改 MainActivity
class MainActivity : ComponentActivity() {
    private lateinit var luckyWheelView: LuckyWheelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GifsicleTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Background image and glass effect
                        AndroidView(
                            factory = { context ->
                                GlassEffectView(context).apply {
                                    setBackgroundImage(context.getDrawable(R.drawable.hs)!!)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Original content
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Greeting("Android")
                            ClickButton(onClickEvent = ::onClickHandler)

                            // Add LuckyWheelView here
                            AndroidView(
                                factory = { context ->
                                    LuckyWheelView(context).apply {
                                        layoutParams = ViewGroup.LayoutParams(600, 600)
                                    }
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            ) { view ->
                                luckyWheelView = view
                            }

                            // Add a button to spin the wheel
                            Button(
                                onClick = {
                                    val targetNumber = (1..6).random()
                                    luckyWheelView.spin(targetNumber)
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Spin the Wheel")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Composable
    fun ClickButton(onClickEvent: () -> Unit) {
        Button(onClick = onClickEvent) {
            Text(text = "Click me!")
        }
    }

    private fun onClickHandler() {
        println("Button clicked!")

        val gifsicle = File(
            File(applicationInfo.nativeLibraryDir),
            "libgifsicle.so"
        )
        if (gifsicle.exists()) {
            Log.d("el", "onClickHandler: ")
        }
        Log.d("el", "applicationInfo.nativeLibraryDir: " + applicationInfo.nativeLibraryDir)
        if (!gifsicle.canExecute()) {
            Log.e("el", "startCustomizeCompress: can't execute")
        }

        val envp = arrayOf("LD_LIBRARY_PATH=" + File(applicationInfo.nativeLibraryDir))
        val cmd = String.format(
            Locale.US,
            "%s -i %s -k 32 -O3 -o %s",
            gifsicle.path,
            File(cacheDir, "test.gif").toString(),
            File(cacheDir, "result.gif").toString()
        )
        Log.i("el", "startCustomizeCompress: envp=${envp[0]}\ncmd=$cmd")
        val process = Runtime.getRuntime().exec(cmd, envp)
        try {
            if (process.waitFor() != 0) {
                Log.e("el", "startCustomizeCompress: running error process.waitFor() != 0")
            } else {
                Log.i("el", "Success!")
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}