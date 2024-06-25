package com.pq.nettyproxy.service.handler

import com.pq.nettyproxy.internal.bean.RequestPackage
import com.pq.nettyproxy.internal.call.NPHttpCallPool
import com.pq.nettyproxy.service.interceptor.RequestInterceptorChain
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.HttpObject

/**
 * @author  calesq
 * @date    2024/6/23
 **/
internal class NPRawHttpRequestHandler(
    private val callPool: NPHttpCallPool
) : SimpleChannelInboundHandler<HttpObject>() {


    override fun acceptInboundMessage(msg: Any?): Boolean {
        return super.acceptInboundMessage(msg)
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: HttpObject?) {

        ctx ?: return
        msg ?: return

        val requestPackage = RequestPackage(ctx.channel(), callPool, msg)

        RequestInterceptorChain.defaultChain().handle(requestPackage)
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {

        ctx ?: return
        callPool.remove(ctx.channel())
        super.channelInactive(ctx)
    }
}