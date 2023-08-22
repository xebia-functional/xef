package com.xebia.functional.xef.tracing

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.images.ImageGenerationUrl
import com.xebia.functional.xef.store.Memory
import kotlin.jvm.JvmInline
import kotlin.reflect.KProperty

typealias Dispatcher = Dispatch<Event>

operator fun Dispatcher?.invoke(event: Event) {
  this?.invoke(event)
}

inline operator fun Dispatcher?.getValue(thisObj: Any?, property: KProperty<*>): Dispatcher =
  this ?: Dispatcher { it }

inline operator fun Dispatcher?.setValue(thisObj: Any?, property: KProperty<*>, value: Event) {
  this?.invoke(value)
}

fun interface Dispatch<A : Event> {
  operator fun invoke(event: A): A
}

fun createDispatcherWithLog(vararg trackers: Tracker): Dispatch<Event> =
  createDispatcher(Tracker.Default, *trackers)

fun createDispatcher(vararg trackers: Tracker): Dispatch<Event> =
  trackers.foldRight(Dispatch { it }) { tracker, dispatcher ->
    Dispatch { tracker(dispatcher, it) }
  }

fun interface Tracker {
  operator fun invoke(next: Dispatcher, event: Event): Event

  companion object {
    val Default: Tracker = Tracker<Generic> {
      when (this) {
        is Messages -> {
          println(" ---- memories")
          memories.forEach { message ->
            println("${message.approxTokens} : ${message.content}")
          }
          println(" ---- contextAllowed")
          contextAllowed.forEach { message ->
            println("${message.role.name} : ${message.content}")
          }
          println(" ---- historyAllowed")
          historyAllowed.forEach { message ->
            println("${message.role.name} : ${message.content}")
          }
          println(" ---- prompt")
          prompt.forEach { message ->
            println("${message.role.name} : ${message.content}")
          }
          println(" ---- tokens")
          println("remainingTokensForContexts : $remainingTokensForContexts")
          println("maxHistoryTokens : $maxHistoryTokens")
          println("maxContextTokens : $maxContextTokens")
          println("countTokens : $countTokens")
          println()
        }
        is ContextAllowed -> {
          println("Context")
          name.forEach { message ->
            println("${message.role.name} : ${message.content}")
          }
          println()
        }
        is Memories -> {
          println("Memories")
          name.forEach { message ->
            println("${message.approxTokens} : ${message.content}")
          }
          println()
        }
        is HistoryAllowed -> {
          println("History")
          name.forEach { message ->
            println("${message.role.name} : ${message.content}")
          }
          println()
        }
        is ImagesRequest -> {
          println("ImagesRequest")
          prompt.forEach { message ->
            println("${message.role.name} : ${message.content}")
          }
          println("numberImages : $numberImages")
          println("size : $size")
          println()
        }
        is ImagesResponse -> {
          println("ImagesResponse")
          urls.forEach { url ->
            println(url.url)
          }
          println()
        }
        is ToolEvent -> {
          println("ToolEvent")
          println("info : $info")
          println()
        }
      }
    }
  }
}

inline fun <reified A : Event> Tracker(crossinline block: A.() -> Unit): Tracker =
  Tracker { next, event ->
    if (event is A) {
      block(event)
      next(event)
    } else {
      next(event)
    }
  }

interface Event

//@JvmInline
//value class Raw<A>(
//  val value: A
//) : Generic

sealed interface Generic : Event

data class ToolEvent(
  val info : String
): Generic

@JvmInline
value class HistoryAllowed(
  val name: List<Message>
) : Generic

@JvmInline
value class Memories(
  val name: List<Memory>
) : Generic


data class Messages(
  val memories: List<Memory>,
  val historyAllowed: List<Message>,
  val contextAllowed: List<Message>,
  val prompt: List<Message>,
  val remainingTokensForContexts: Int,
  val maxHistoryTokens: Int,
  val maxContextTokens: Int,
  val countTokens: Int,
) : Generic

@JvmInline
value class ContextAllowed(
  val name: List<Message>
) : Generic

data class ImagesRequest(
  val prompt: List<Message>,
  val numberImages: Int,
  val size: String,
) : Generic

data class ImagesResponse(
  val urls: List<ImageGenerationUrl>,
) : Generic