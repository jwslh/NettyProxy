package com.pq.nettyproxy.internal.bean

import com.pq.nettyproxy.internal.call.NPCall
import com.pq.nettyproxy.internal.call.NPHttpCallPool
import io.netty.channel.Channel
import io.netty.handler.codec.http.HttpObject

internal data class RequestPackage(
    val channel: Channel,
    val callPool: NPHttpCallPool,
    val msg: HttpObject,
    var call: NPCall? = null,
) {
    fun tryGetCall(): NPCall {
        return call ?: run {
            throw NullPointerException("Call is null. There is any bug.")
        }
    }
}
