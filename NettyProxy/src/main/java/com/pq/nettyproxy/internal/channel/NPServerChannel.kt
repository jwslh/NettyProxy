package com.pq.nettyproxy.internal.channel

import com.pq.nettyproxy.internal.call.NPCall
import com.pq.nettyproxy.internal.call.NPRoute
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture

/**
 * @author  calesq
 * @date    2024/6/23
 **/
internal class NPServerChannel(
    val route: NPRoute,
    private var rawChannel: Channel,
    val isMultiplexed: Boolean = false,
) {
    private var allocationLimit = 1
    @Volatile
    private var disable = false

    val calls = mutableListOf<NPCall>()

    fun isEligible(host: String, port: Int): Boolean {
        synchronized(calls) {
            if (disable || calls.size >= allocationLimit || !rawChannel.isActive) {
                return false
            }
        }

        if (route.host != host || route.port != port) {
            return false
        }
        return true
    }

    fun writeAndFlush(msg: Any): ChannelFuture {
        return rawChannel.writeAndFlush(msg)
    }

    fun close(msg: Any): ChannelFuture {
        return rawChannel.close()
    }

    fun isActive() = rawChannel.isActive

    fun isTargetChannel(channel: Channel): Boolean {
        return channel == rawChannel
    }

    fun notifyChannelClosed() {
        synchronized(calls) {
            calls.forEach {
                it.forClientChannel.close().sync()
            }
            disable = true
            calls.clear()
        }
    }
}