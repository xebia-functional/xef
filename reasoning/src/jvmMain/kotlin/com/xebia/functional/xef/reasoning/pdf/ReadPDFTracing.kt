package com.xebia.functional.xef.reasoning.pdf

import com.xebia.functional.xef.tracing.Event

sealed interface ReadPDFTracing : Event {
    data class ReadingUrl(val url: String) : ReadPDFTracing
}