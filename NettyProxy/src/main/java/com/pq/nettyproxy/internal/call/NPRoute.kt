package com.pq.nettyproxy.internal.call

data class NPRoute(
    val host: String,
    val port: Int,
) {
    override fun equals(other: Any?): Boolean {
        return other is NPRoute
                && other.host == host
                && other.port == port
    }

    override fun hashCode(): Int {
        var result = 17
        result += 31 * result + host.hashCode()
        result += 31 * result + port
        return result
    }
}
