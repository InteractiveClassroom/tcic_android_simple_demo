//package com.example.tcic_android_simple_demo
//
//import android.os.Bundle
//import android.webkit.JavascriptInterface
//import android.webkit.WebResourceRequest
//import android.webkit.WebSettings
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import com.qcloudclass.tcic.TCICConfig
//import com.qcloudclass.tcic.TCICHeaderComponentConfig
//import com.qcloudclass.tcic.TCICManager
//import com.qcloudclass.tcic.TCICManager.TCICCallback
//import org.json.JSONObject
//
//
//class MainActivity : ComponentActivity() {
//
//    private lateinit var webView: WebView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        initTCICSDK();
//        webView = WebView(this)
//        setupWebView()
//        setupWebViewClient()
//
//        webView.addJavascriptInterface(WebAppInterface(), "Android")
//        webView.loadUrl("https://dev-class.qcloudclass.com/flutter/login.html?lng=zh")
//
//        setContentView(webView)
//    }
//
//    private fun initTCICSDK() {
//        TCICManager.initialize(this@MainActivity);
//        TCICManager.setCallback(object : TCICCallback {
//            override fun onJoinedClassSuccess() {
//                runOnUiThread {
//                    Toast.makeText(this@MainActivity, "加入课堂成功", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun afterExitedClass() {
//                runOnUiThread {
//                    Toast.makeText(this@MainActivity, "已退出课堂，关闭页面", Toast.LENGTH_SHORT).show()
//                    TCICManager.closeFlutterActivity() // 关闭当前 Activity
//                }
//            }
//
//            override fun onJoinedClassFailed() {
//                runOnUiThread {
//                    Toast.makeText(this@MainActivity, "加入课堂失败", Toast.LENGTH_SHORT).show()
//                    TCICManager.closeFlutterActivity() // 关闭当前 Activity
//                }
//            }
//
//            override fun onKickedOffClass() {
//                runOnUiThread {
//                    Toast.makeText(this@MainActivity, "被踢出课堂", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onMemberJoinedClass(data: Map<*, *>) {
//                runOnUiThread {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "成员加入: $data",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//
//            override fun onMemberLeaveClass(data: Map<*, *>) {
//                runOnUiThread {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "成员离开: $data",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//
//            override fun onRecivedMessage(message: Map<*, *>) {
//                runOnUiThread {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "收到消息: $message",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//
//            override fun onError(errorCode: String, errorMsg: String) {
//                runOnUiThread {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "错误: $errorMsg",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        })
//    }
//
//    private fun setupWebView() {
//        val webSettings = webView.settings
//        webSettings.javaScriptEnabled = true
//        webSettings.domStorageEnabled = true
//        webSettings.databaseEnabled = true
//        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
//        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//        webSettings.userAgentString = "YourApp/1.0 " + webSettings.userAgentString
//
//        WebView.setWebContentsDebuggingEnabled(true)
//    }
//
//    private fun setupWebViewClient() {
//        webView.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(
//                view: WebView?,
//                request: WebResourceRequest?
//            ): Boolean {
//                return false // 让WebView自己处理
//            }
//
//            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
//                super.onPageStarted(view, url, favicon)
//                android.util.Log.d("WebView", "开始加载: $url")
//            }
//
//            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
//                android.util.Log.d("WebView", "加载完成: $url")
//            }
//
//            override fun onReceivedError(
//                view: WebView?,
//                errorCode: Int,
//                description: String?,
//                failingUrl: String?
//            ) {
//                super.onReceivedError(view, errorCode, description, failingUrl)
//                android.util.Log.e("WebView", "加载错误: $description")
//                runOnUiThread {
//                    Toast.makeText(this@MainActivity, "页面加载失败: $description", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        webView.destroy()
//        super.onDestroy()
//    }
//
//    inner class WebAppInterface {
//
//        @JavascriptInterface
//        fun gotoRoomPage(jsonString: String) {
//            runOnUiThread {
//                val jsonObject = JSONObject(jsonString);
//                val headerComponentConfig = TCICHeaderComponentConfig()
//                headerComponentConfig.setHeaderLeftBuilder { HeaderLeftViewCreator() }
//                val userid = jsonObject.optString("userid", "")
//                val classId = jsonObject.optString("classid", "")
//                val token = jsonObject.optString("token", "")
//                val role = jsonObject.optString("role", "student");
//                val realRole = when (role) {
//                    "student" -> 0
//                    "teacher" -> 1
//                    "assistant" -> 3
//                    else -> 4
//                } /// 用户角色，0: 学生,1: 老师, 3: 助教, 4: 巡课
//
//                val config = TCICConfig(token, classId, userid, realRole);
//                config.headerComponentConfig = headerComponentConfig;
//                TCICManager.setConfig(config);
//                 val intent = TCICManager.getTCICIntent(this@MainActivity);
//                 startActivity(intent)
//            }
//        }
//    }
//}

package com.example.tcic_android_simple_demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onContentChanged() {
        super.onContentChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 直接启动向导页面
        val intent = Intent(this, ClassroomSetupWizardActivity::class.java)
        startActivity(intent)
        finish()
    }
}