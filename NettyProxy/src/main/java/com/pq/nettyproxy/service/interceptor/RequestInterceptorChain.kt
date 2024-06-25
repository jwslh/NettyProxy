package com.pq.nettyproxy.service.interceptor

/**
 * @author  calesq
 * @date    2024/6/23
 **/
internal object RequestInterceptorChain {
    fun defaultChain(): AbsRequestInterceptor {
        val first = PreCheckInterceptor()
        first.next(TransmitterInterceptor())
        return first
    }
}
