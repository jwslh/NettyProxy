package com.pq.nettyproxy.internal.proxy.handler

import com.pq.nettyproxy.internal.channel.NPServerChannelPool
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.HttpObject

/**
 * @author  calesq
 * @date    2024/6/23
 **/
internal class NPProxyHttpRequestHandler(
    val serverChannelPool: NPServerChannelPool
) : SimpleChannelInboundHandler<HttpObject>() {


    override fun acceptInboundMessage(msg: Any?): Boolean {
        return super.acceptInboundMessage(msg)
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: HttpObject?) {
        ctx ?: return
        msg ?: return
        val forServerChannel = ctx.channel()
        sendToClient(forServerChannel, msg)
    }

    private fun sendToClient(forServerChannel: Channel, msg: HttpObject) {
        serverChannelPool.writeAndFlushToClient(forServerChannel, msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {

        ctx ?: return
        val channel = ctx.channel()

        // 先找到 serverChannelPool 中的所有call
        serverChannelPool.notifyChannelClosed(channel)

        super.channelInactive(ctx)
    }
}