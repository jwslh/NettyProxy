package com.pq.nettyproxy.internal.proxy.client

import com.pq.nettyproxy.internal.bean.ClientType
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.Throws

/**
 * @author  calesq
 * @date    2024/6/23
 **/
internal object NPClientManager {

    private val mClientPool: ConcurrentHashMap<ClientType, NPClient> = ConcurrentHashMap<ClientType, NPClient>()

    fun init() {
        ClientType.values().forEach {
            mClientPool[it] = NPClient.build().apply { start() }
        }
    }

    @Throws
    fun get(type: ClientType = ClientType.DEFAULT): NPClient {
        return mClientPool[type] ?: run {
            throw RuntimeException("The client of the target type[$type] is null.")
        }
    }
}