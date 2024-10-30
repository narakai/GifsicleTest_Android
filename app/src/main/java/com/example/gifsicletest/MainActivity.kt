package com.example.gifsicletest

import GlassEffectView
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex

// 玻璃效果修饰符
fun Modifier.glassEffect(
    alpha: Float = 0.8f,
    spacing: Float = 8f,
) = this.drawWithCache {
    val verticalLines = mutableListOf<Float>()
    var x = 0f
    while (x < size.width) {
        verticalLines.add(x)
        x += spacing
    }

    onDrawBehind {
        // 绘制半透明背景
        drawRect(
            color = Color.White.copy(alpha = 0.2f),
            blendMode = BlendMode.Screen
        )

        // 绘制垂直线条
        for (lineX in verticalLines) {
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(lineX, 0f),
                end = Offset(lineX, size.height),
                strokeWidth = 1f,
                blendMode = BlendMode.Screen
            )
        }
    }
}

// 修改 MainActivity
class MainActivity : ComponentActivity() {
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