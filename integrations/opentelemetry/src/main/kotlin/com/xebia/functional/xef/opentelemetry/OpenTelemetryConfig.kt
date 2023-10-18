package com.xebia.functional.xef.opentelemetry

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
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
        val jaegerOtlpExporter: OtlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(endpointConfig)
            .setTimeout(30, TimeUnit.SECONDS)
            .build()

        val serviceNameResource: Resource =
            Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), serviceName))

        val tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(jaegerOtlpExporter).build())
            .setResource(Resource.getDefault().merge(serviceNameResource))
            .build()


        val openTelemetry = OpenTelemetrySdk
            .builder()
            .setTracerProvider(tracerProvider)
            .build()

        Runtime.getRuntime().addShutdownHook(Thread { tracerProvider.close() })
        return openTelemetry
    }

    companion object {
        val DEFAULT = OpenTelemetryConfig(
            endpointConfig = "http://localhost:4317",
            defaultScopeName = "io.xef",
            serviceName = "xef"
        )
    }
}
