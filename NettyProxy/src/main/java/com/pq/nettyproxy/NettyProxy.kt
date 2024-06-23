package com.pq.nettyproxy

import kotlin.concurrent.thread

/**
 * @author  calesq
 * @date    2024/6/23
 **/
object NettyProxy {

    /**
     * 启动代理服务
     */
    fun start(config: NettyProxyConfig) {

        config.serverList.forEach { builder ->
            thread(name = "npserver-${builder.host}-$${builder.port}") {
                builder.build().start()
            }
        }
    }

}
