package com.xebia.functional.xef.opentelemetry

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import java.util.concurrent.TimeUnit

data class OpenTelemetryConfig(
  val endpointConfig: String,
  val defaultScopeName: String,
  val serviceName: String
) {

  fun newInstance(): OpenTelemetry {
    val otlpGrpcSpanExporter: OtlpGrpcSpanExporter =
      OtlpGrpcSpanExporter.builder()
        .setEndpoint(endpointConfig)
        .setTimeout(30, TimeUnit.SECONDS)
        .build()

    val resource =
      Resource.getDefault()
        .toBuilder()
        .put(AttributeKey.stringKey("service.name"), serviceName)
        .build()

    val tracerProvider =
      SdkTracerProvider.builder()
        .addSpanProcessor(BatchSpanProcessor.builder(otlpGrpcSpanExporter).build())
        .setResource(resource)
        .build()

    val meterProvider =
      SdkMeterProvider.builder()
        .registerMetricReader(
          PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build()
        )
        .setResource(resource)
        .build()

    val openTelemetry =
      OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setMeterProvider(meterProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .buildAndRegisterGlobal()

    Runtime.getRuntime().addShutdownHook(Thread { openTelemetry.close() })
    return openTelemetry
  }

  companion object {
    fun create(serviceName: String, defaultScopeName: String) =
      OpenTelemetryConfig(
        endpointConfig = "http://localhost:4317",
        defaultScopeName = defaultScopeName,
        serviceName = serviceName
      )
  }
}
