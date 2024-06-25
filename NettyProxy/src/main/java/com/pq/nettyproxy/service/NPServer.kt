package com.pq.nettyproxy.service

import com.pq.common.utils.NPLog
import com.pq.nettyproxy.internal.call.NPHttpCallPool
import com.pq.nettyproxy.service.handler.NPRawHttpRequestHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate
import io.netty.handler.stream.ChunkedWriteHandler

/**
 * @author  calesq
 * @date    2024/6/23
 **/
class NPServer private constructor(builder: Builder){

    private val host = builder.host
    private val port = builder.port

    private var mServerBootstrap: ServerBootstrap? = null
    private val mSslContext by lazy {
        val cert = SelfSignedCertificate()
        SslContextBuilder.forServer(cert.key(), cert.cert()).build()
    }

    private val callPool by lazy {
        NPHttpCallPool()
    }

    fun start() {
        val bossEventLoopGroup = NioEventLoopGroup()
        val workerEventLoopGroup = NioEventLoopGroup()

        try {

            val serverBootstrap = ServerBootstrap()
            mServerBootstrap = serverBootstrap
            NPLog.i(TAG, "start server")
            serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_BACKLOG, 64)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel?) {
                        ch ?: return
                        ch.pipeline().apply {
                            NPLog.i(TAG, "init channel")
                            addLast(mSslContext.newHandler(ch.alloc())) // 自签名证书配置SSL，支持HTTPS通信
                            addLast(HttpServerCodec())
                            addLast(ChunkedWriteHandler())
                            addLast(NPRawHttpRequestHandler(callPool))
                        }
                    }
                })
            serverBootstrap.bind(host, port).sync().channel().closeFuture().sync()
        } catch (e: Exception) {
            NPLog.e(TAG, "Failed to run server bootstrap.", e)
        } finally {
            bossEventLoopGroup.shutdownGracefully()
            workerEventLoopGroup.shutdownGracefully()
            callPool.clear()
            mServerBootstrap = null
        }
    }


    class Builder {

        var host : String = ""
        var port : Int = 0

        fun build() : NPServer {
            return NPServer(this)
        }
    }

    companion object {

        private const val TAG = "NPServer"

        fun build(builder: Builder.() -> Unit = {}) : NPServer {
            return Builder().apply(builder).build()
        }

    }
}