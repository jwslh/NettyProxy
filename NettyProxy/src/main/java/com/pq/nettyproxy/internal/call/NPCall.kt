package com.pq.nettyproxy.internal.call

import com.pq.nettyproxy.internal.channel.NPServerChannel
import io.netty.channel.Channel

internal data class NPCall(
    val host: String,
    val port: Int,
    val forClientChannel: Channel,
    val isProxyRequest: Boolean = false,
    val proxyHost: String = "",
    val proxyPort: Int = 0,
    @Volatile
    private var mServerChannel: NPServerChannel? = null
) {

    fun acquireConnection(channel: NPServerChannel) {
        mServerChannel = channel
        synchronized(channel.calls) {
            channel.calls.clear()
            channel.calls.add(this)
        }
    }

    fun getServerChannel() = mServerChannel
}
