package com.pq.common.interceptor


/**
 * 实现拦截器主要有两个处理方式：
 * 1. 通过链表去维护每一个拦截器, 本次使用这个
 * 2. 通过数组去维护每一个拦截器（如： OKHttp）
 */
interface IInterceptor<T, R> {

    fun next(interceptor: IInterceptor<T, R>) : IInterceptor<T, R>

    fun handle(msg: T?): R
}