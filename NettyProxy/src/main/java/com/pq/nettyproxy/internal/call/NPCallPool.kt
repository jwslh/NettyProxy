package com.pq.nettyproxy.internal.call

import io.netty.channel.Channel
import java.util.concurrent.ConcurrentHashMap

/**
 * @author  calesq
 * @date    2024/6/23
 **/
internal class NPHttpCallPool {

    private val calls: ConcurrentHashMap<Channel, NPCall> = ConcurrentHashMap<Channel, NPCall>()

    fun storeCallForChannel(channel: Channel, call: NPCall) {
        calls[channel] = call
    }

    fun getCallForChannel(channel: Channel): NPCall? {
        return calls[channel]
    }

    fun clear() {
        calls.clear()
    }

    fun remove(channel: Channel) {
        calls.remove(channel)
    }
}
