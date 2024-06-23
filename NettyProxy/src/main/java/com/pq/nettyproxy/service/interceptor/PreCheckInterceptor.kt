package com.pq.nettyproxy.service.interceptor

import com.pq.common.constant.HeadersName
import com.pq.common.constant.NPConstant
import com.pq.common.utils.NPLog
import com.pq.nettyproxy.internal.bean.RequestPackage
import com.pq.nettyproxy.internal.call.NPCall
import io.netty.channel.Channel
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpRequest
import java.net.URI

/**
 * @author  calesq
 * @date    2024/6/23
 **/
internal class PreCheckInterceptor : AbsRequestInterceptor() {

    override fun handle0(requestPackage: RequestPackage): Boolean {

        tryToHandleHttpRequest(requestPackage)
        tryToHandleHttpContent(requestPackage)

        if (checkCallNull(requestPackage)) {
            NPLog.e(
                TAG,
                "Current httpObject does not have target pooled call. It means this application has any bug."
            )
            return false
        }

        return nextInterceptor?.handle(requestPackage) ?: true
    }

    private fun checkCallNull(requestPackage: RequestPackage): Boolean {
        return requestPackage.call == null
    }

    private fun tryToHandleHttpRequest(requestPackage: RequestPackage) {
        if (requestPackage.msg !is HttpRequest) return

        val call = handleRequest(requestPackage.msg, requestPackage.channel)

        requestPackage.callPool.storeCallForChannel(requestPackage.channel, call)

        requestPackage.call = call
    }

    private fun handleRequest(httpRequest: HttpRequest, channel: Channel): NPCall {
        val headers = httpRequest.headers()

        var realHost = headers.get(HeadersName.REAL_HOST, "")
        var realPort = headers.getInt(HeadersName.REAL_PORT, 0)

        if (realHost.startsWith(NPConstant.PREFIX_HTTP)) {
            val uri = URI(realHost)
            realHost = uri.host
            val port = uri.port

            if (realPort <= 0 && port >= 0) {
                realPort = port
            }
        }

        headers.set(HttpHeaderNames.HOST, "$realHost:$realPort")

        val call = NPCall(realHost, realPort, channel)
        return call
    }

    private fun tryToHandleHttpContent(requestPackage: RequestPackage) {
        if (requestPackage.msg !is HttpContent) return

        val call = requestPackage.callPool.getCallForChannel(requestPackage.channel)
        requestPackage.call = call
    }

    companion object {
        private const val TAG = "PreCheckInterceptor"
    }
}
