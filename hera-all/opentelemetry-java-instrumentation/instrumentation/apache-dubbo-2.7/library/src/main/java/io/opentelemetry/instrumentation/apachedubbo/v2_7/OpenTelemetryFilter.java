/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.apachedubbo.v2_7;

import static io.opentelemetry.api.trace.SpanKind.CLIENT;
import static io.opentelemetry.api.trace.SpanKind.SERVER;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.concurrent.CompletableFuture;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.support.RpcUtils;

@Activate(group = {"consumer", "provider"})
public class OpenTelemetryFilter implements Filter {
    private final DubboTracer tracer;

    public OpenTelemetryFilter() {
        this.tracer = new DubboTracer();
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        if (!(invocation instanceof RpcInvocation)) {
            return invoker.invoke(invocation);
        }
        String methodName = invocation.getMethodName();
        String interfaceName = invoker.getInterface().getName();
        RpcContext rpcContext = RpcContext.getContext();
        SpanKind kind = rpcContext.isProviderSide() ? SERVER : CLIENT;
        final Context context;
        if (kind.equals(CLIENT)) {
            context = tracer.startClientSpan(interfaceName, methodName, invoker.getUrl());
            tracer.inject(context, (RpcInvocation) invocation, DubboInjectAdapter.SETTER);
            RpcContext.getContext().getAttachments().put("traceparent", invocation.getAttachment("traceparent"));
        } else {
            context = tracer.startServerSpan(interfaceName, methodName, (RpcInvocation) invocation);
        }
        final Result result;
        boolean isSynchronous = true;
        try (Scope ignored = context.makeCurrent()) {
            result = invoker.invoke(invocation);
            if (kind.equals(CLIENT)) {
                CompletableFuture<Object> future = rpcContext.getCompletableFuture();
                boolean async = RpcUtils.isAsync(invoker.getUrl(), invocation);
                if (future != null && async) {
                    isSynchronous = false;
                    future.whenComplete((o, throwable) -> {
                        Object bizResult;
                        if (throwable != null) {
                            tracer.endExceptionally(context, throwable);
                        } else {
                            // 处理结果
                            if (o instanceof AsyncRpcResult) {
                                AsyncRpcResult asyncResult = (AsyncRpcResult) o;
                                bizResult = asyncResult.getValue();
                            } else {
                                bizResult = o;
                            }
                            tracer.end(context, bizResult);
                        }
                    });
                }
            }

        } catch (Throwable e) {
            tracer.endExceptionally(context, e);
            throw e;
        }
        if (isSynchronous) {
            if (result.hasException()) {
                tracer.endExceptionally(context, result.getException());
            } else {
                tracer.parseBussinessCode(context, result);
                tracer.end(context, result);
            }
        }
        return result;
    }
}
