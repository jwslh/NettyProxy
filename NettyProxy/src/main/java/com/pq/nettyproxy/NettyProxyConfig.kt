package com.pq.nettyproxy

import com.pq.nettyproxy.service.NPServer

/**
 * @author  calesq
 * @date    2024/6/23
 **/
class NettyProxyConfig {

    internal val serverList: MutableList<NPServer.Builder> = mutableListOf()

    fun addServer(builder: NPServer.Builder.() -> Unit): NettyProxyConfig {
        serverList.add(NPServer.Builder().apply(builder))
        return this
    }
}