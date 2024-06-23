package com.pq.nettyproxy.service.interceptor

import com.pq.common.interceptor.IInterceptor
import com.pq.nettyproxy.internal.bean.RequestPackage
import io.netty.handler.codec.http.HttpObject

/**
 * @author  calesq
 * @date    2024/6/23
 **/
internal abstract class AbsRequestInterceptor : IInterceptor<RequestPackage, Boolean> {
    protected var nextInterceptor: IInterceptor<RequestPackage, Boolean>? = null

    override fun next(interceptor: IInterceptor<RequestPackage, Boolean>): IInterceptor<RequestPackage, Boolean> {
        nextInterceptor = interceptor
        return interceptor
    }

    override fun handle(msg: RequestPackage?): Boolean {
        return if (msg == null) {
            true
        } else {
            handle0(msg)
        }
    }

    abstract fun handle0(msg: RequestPackage): Boolean
}
