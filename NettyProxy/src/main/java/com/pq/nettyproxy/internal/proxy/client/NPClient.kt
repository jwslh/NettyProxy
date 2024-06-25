package com.pq.nettyproxy.internal.proxy.client

import com.pq.common.utils.NPLog
import com.pq.nettyproxy.internal.call.NPCall
import com.pq.nettyproxy.internal.call.NPRoute
import com.pq.nettyproxy.internal.channel.NPServerChannel
import com.pq.nettyproxy.internal.channel.NPServerChannelPool
import com.pq.nettyproxy.internal.proxy.handler.NPProxyHttpRequestHandler
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.handler.stream.ChunkedWriteHandler
import kotlin.jvm.Throws

/**
 * @author  calesq
 * @date    2024/6/23
 **/
internal class NPClient private constructor(builder: Builder) {

    private var mBootstrap: Bootstrap? = null

    private val mServerChannelPool by lazy {
        NPServerChannelPool()
    }

    private val sslContext by lazy {
        SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build()
    }

    fun start() {
        val workerEventLoopGroup = NioEventLoopGroup()
        try {

            val bootstrap = Bootstrap()
            mBootstrap = bootstrap

            bootstrap.group(workerEventLoopGroup)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000) // 10s的请求连接时间
                .handler(object : ChannelInitializer<NioSocketChannel>() {
                    override fun initChannel(ch: NioSocketChannel?) {
                        ch ?: return
                        ch.pipeline().apply {
                            addLast(sslContext.newHandler(ch.alloc()))
                            addLast(HttpClientCodec())
                            addLast(ChunkedWriteHandler())
                            addLast(NPProxyHttpRequestHandler(mServerChannelPool))
                        }
                    }
                })

        } catch (e: Exception) {
            NPLog.e(TAG, "Create client bootstrap failed.", e)
        } finally {
            mBootstrap = null
        }
    }

    /**
     * Find or create channel to communicate with endpoint.
     */
    @Throws
    fun findChannel(call: NPCall): NPServerChannel {
        val bootstrap = mBootstrap
        checkNotNull(bootstrap)

        val pooledChannel = call.getServerChannel()
        if (pooledChannel != null) {
            return pooledChannel
        }

        val result = mServerChannelPool.callAcquirePooledConnection(call)

        if (!result) {
            val rawChannel = bootstrap.connect(call.host, call.port).sync().channel()
            val serverChannel = NPServerChannel(NPRoute(call.host, call.port), rawChannel)
            call.acquireConnection(serverChannel)
            mServerChannelPool.put(serverChannel)
        }
        return call.getServerChannel()
            ?: throw NullPointerException("After findChannel(), the server channel still is null.")
    }

    class Builder {

        fun build(): NPClient {
            return NPClient(this)
        }
    }

    companion object {

        private const val TAG = "NPClient"

        fun build(builder: Builder.() -> Unit = {}): NPClient {
            return Builder().apply(builder).build()
        }

    }
}
