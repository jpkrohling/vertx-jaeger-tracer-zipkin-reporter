package com.example.demo;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.reporters.CompositeReporter;
import io.jaegertracing.internal.reporters.LoggingReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.zipkin.ZipkinV2Reporter;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

public class MainVerticle extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new MainVerticle());
    }

    @Override
    public void start() throws Exception {
        ZipkinV2Reporter zipkinReporter = new ZipkinV2Reporter(AsyncReporter.create(URLConnectionSender.create("http://localhost:9411/api/v2/spans")));
        CompositeReporter reporter = new CompositeReporter(zipkinReporter, new LoggingReporter());

        Tracer tracer = new JaegerTracer.Builder("serviceName")
        .withSampler(new ConstSampler(true))
        .withReporter(reporter)
        .build();

        vertx.createHttpServer().requestHandler(req -> {
            try (Scope scope = tracer.buildSpan("operation").startActive(true)) {
                System.out.println("Request received");
                req.response()
                .putHeader("content-type", "text/plain")
                .end("Hello from Vert.x!");
            }
        }).listen(8080);
        System.out.println("HTTP server started on port 8080");
    }
}
