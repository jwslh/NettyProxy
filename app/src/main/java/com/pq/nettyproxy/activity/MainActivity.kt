package com.pq.nettyproxy.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.pq.nettyproxy.NettyProxy
import com.pq.nettyproxy.NettyProxyConfig
import com.pq.nettyproxy.R
import com.pq.nettyproxy.databinding.ActivityMainBinding
import com.pq.nettyproxy.net.API
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.bt_start_server -> {
                startServer()
            }
            R.id.bt_send -> {
                send()
            }
            else -> {}
        }
    }

    private fun startServer() {
        val config = NettyProxyConfig().addServer {
            host = "localhost"
            port = 10086
        }

        NettyProxy.start(config)
    }

    private fun send() {
        val request = Request.Builder()
                            .get()
                            .url("https://localhost:10086/test")
                            .build()
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(this::class.simpleName, API.send(request).toString())
        }
    }
}