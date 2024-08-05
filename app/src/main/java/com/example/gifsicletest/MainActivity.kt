package com.example.gifsicletest

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
import androidx.compose.ui.tooling.preview.Preview
import com.example.gifsicletest.ui.theme.GifsicleTestTheme
import java.io.File
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GifsicleTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    ClickButton(onClickEvent = ::onClickHandler)
                }
            }
        }
    }

    private fun onClickHandler() {
        // 处理点击事件
        println("Button clicked!")

//        try {
//            val nativeLibraryDir = File(applicationInfo.nativeLibraryDir)
//            val primaryNativeLibraries = nativeLibraryDir.list()
//            val count = primaryNativeLibraries.size
//        } catch (e: Exception) {
//
//        }

        val gifsicle = File(File(applicationInfo.nativeLibraryDir), "libgifsicle.so")   //可执行文件地址安装后形如：/data/app/com.equationl.myapplication-wZxpZo7IgVPNv3jvY0S8QA==/lib/arm/libgifsicle.so
        if (gifsicle.exists()) {
            Log.d("el", "onClickHandler: ")
        }
        Log.d("el", "applicationInfo.nativeLibraryDir: " + applicationInfo.nativeLibraryDir)
        if (!gifsicle.canExecute()) {   //无法执行该执行文件
            Log.e("el", "startCustomizeCompress: can't excute")
        }

        val envp = arrayOf("LD_LIBRARY_PATH=" + File(applicationInfo.nativeLibraryDir))  //设置环境
        val cmd = String.format(Locale.US, "%s -i %s -k 32 -O3 -o %s",
            gifsicle.path, File(cacheDir, "test.gif").toString(), File(cacheDir, "result.gif").toString())  //设置命令，此处作用为将缓存目录下的 test.gif 更改颜色数为256 按第3级别优化并输出至缓存目录下 result.gif （详细请自己看 gifsicle 的文档）
        Log.i("el", "startCustomizeCompress: envp=${envp[0]}\ncmd=$cmd")
        val process = Runtime.getRuntime().exec(cmd, envp) //开始执行命令
        try {
            if (process.waitFor() != 0) {  //如果执行成功会返回 0，不成功返回非0
                Log.e("el", "startCustomizeCompress: running error process.waitFor() != 0")
            }
            else {
                Log.i("el", "Success!")
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GifsicleTestTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Greeting("Android")
            ClickButton { println("Button clicked in preview!") }
        }
    }
}