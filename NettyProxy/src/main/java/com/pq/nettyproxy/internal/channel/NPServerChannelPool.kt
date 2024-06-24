package com.pq.nettyproxy.internal.channel

import com.pq.common.utils.NPLog
import com.pq.nettyproxy.internal.call.NPCall
import io.netty.channel.Channel
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write

/**
 * 用于缓存 proxy 和 云端之间的 channel，减少连接耗时
 * @author  calesq
 * @date    2024/6/23
 **/
internal class NPServerChannelPool(
    private val maxIdleChannels: Int = 10,
) {
    private val channels = ConcurrentLinkedQueue<NPServerChannel>()

    init {
        thread {
            val timerTask = object : TimerTask() {
                override fun run() {
                    NPLog.i(TAG, "Run cleanup task.")
                    cleanup()
                }
            }
            Timer().schedule(timerTask, 30_000, 60_0000)
        }
    }

    fun idleConnectionCount(): Int {
        return channels.count {
            synchronized(it) {
                synchronized(it.calls) {
                    it.calls.isEmpty()
                }
            }
        }
    }

    fun connectionCount(): Int {
        return channels.size
    }

    fun callAcquirePooledConnection(
        call: NPCall,
        requireMultiplexed: Boolean = false,
    ): Boolean {
        for (channel in channels) {
            synchronized(channel) {
                if (requireMultiplexed && !channel.isMultiplexed) return@synchronized
                if (!channel.isEligible(call.host, call.port)) return@synchronized
                call.acquireConnection(channel)
                return true
            }
        }
        return false
    }

    fun put(serverChannel: NPServerChannel) {
        channels.add(serverChannel)
    }

    fun notifyChannelClosed(serverChannel: Channel) {
        channels.firstOrNull {
            it.isTargetChannel(serverChannel)
        }?.let {
            it.notifyChannelClosed()
            channels.remove(it)
        }
    }

    fun writeAndFlushToClient(serverChannel: Channel, msg: HttpObject) {
        channels.firstOrNull {
            it.isTargetChannel(serverChannel)
        }?.let {
            val realMsg = when (msg) {
                is HttpRequest -> msg
                is HttpContent -> msg.retain()
                else -> return
            }
            synchronized(it.calls) {
                it.calls.firstOrNull()?.forClientChannel?.writeAndFlush(realMsg)
            }
        }
    }

    private fun cleanup() {
        channels.removeIf { channel ->
            !channel.isActive()
        }
    }

    companion object {
        private const val TAG = "NPServerChannelPool"
    }

}