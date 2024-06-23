package com.pq.nettyproxy.internal.bean

import io.netty.channel.Channel
import io.netty.handler.codec.http.HttpObject

data class ResponsePackage(
    val forServerChannel: Channel,
    val msg: HttpObject,
)
