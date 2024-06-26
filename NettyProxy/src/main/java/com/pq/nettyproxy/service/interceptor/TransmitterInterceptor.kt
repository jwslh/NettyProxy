package com.pq.nettyproxy.service.interceptor

import com.pq.common.utils.NPLog
import com.pq.nettyproxy.internal.bean.RequestPackage
import com.pq.nettyproxy.internal.channel.NPServerChannel
import com.pq.nettyproxy.internal.proxy.client.NPClientManager
import io.netty.channel.Channel
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpRequest

/**
 * @author  calesq
 * @date    2024/6/23
 **/
internal class TransmitterInterceptor : AbsRequestInterceptor() {
    override fun handle0(msg: RequestPackage): Boolean {
        send(connect(msg), msg)
        return nextInterceptor?.handle(msg) ?: return true
    }

    private fun connect(requestPackage: RequestPackage): NPServerChannel {

        if (requestPackage.msg is HttpRequest) {
            // 当前clientChannel是被复用的，之前存在请求,直接尝试复用 serverChannelPool 中缓存的已经存在的serverChannel
            requestPackage.callPool.getCallForChannel(requestPackage.channel)?.getServerChannel()?.let {
                val call = requestPackage.call
                if (call != null && it.isEligible(call.host, call.port)) {
                    call.acquireConnection(it)
                }
            }
        }

        return NPClientManager.get().findChannel(requestPackage.tryGetCall())
    }

    private fun send(serverChannel: NPServerChannel, requestPackage: RequestPackage) {
        var msg = requestPackage.msg
        when(msg) {
            is HttpRequest -> {}
            is HttpContent -> msg.retain()
            else -> {
                NPLog.e(TAG, "Invalid msg type. msg=$msg")
                return
            }
        }
        serverChannel.writeAndFlush(msg)
    }

    companion object {
        private const val TAG = "TransmitterInterceptor"
    }
}
