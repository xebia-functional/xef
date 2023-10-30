package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.conversation.llm.openai.*
import com.xebia.functional.xef.conversation.llm.openai.OpenAI.FromEnvironment.*
import com.xebia.functional.xef.conversation.*
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.llm.models.images.*
import com.xebia.functional.xef.metrics.*
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.scala.serialization.*
import com.xebia.functional.xef.store.*
import io.circe.Decoder
import io.circe.parser.parse
import org.reactivestreams.*

import java.util.UUID.*
import java.util.concurrent.LinkedBlockingQueue
import scala.jdk.CollectionConverters.*

class ScalaConversation(store: VectorStore, metric: Metric, conversationId: ConversationId) extends JVMConversation(store, metric, conversationId)

def addContext(context: Array[String])(using conversation: ScalaConversation): Unit =
  conversation.addContextFromArray(context).join()

def prompt[A: Decoder: SerialDescriptor](prompt: Prompt, chat: ChatWithFunctions = DEFAULT_SERIALIZATION)(using conversation: ScalaConversation): A =
  conversation.prompt(chat, prompt, chat.chatFunction(SerialDescriptor[A].serialDescriptor), fromJson).join()

def promptMessage(prompt: Prompt, chat: Chat = DEFAULT_CHAT)(using conversation: ScalaConversation): String =
  conversation.promptMessage(chat, prompt).join()

def promptMessages(prompt: Prompt, chat: Chat = DEFAULT_CHAT)(using conversation: ScalaConversation): List[String] =
  conversation.promptMessages(chat, prompt).join().asScala.toList

def promptStreaming(prompt: Prompt, chat: Chat = DEFAULT_CHAT)(using conversation: ScalaConversation): LazyList[String] =
  val publisher = conversation.promptStreamingToPublisher(chat, prompt)
  val queue = new LinkedBlockingQueue[String]()
  publisher.subscribe(new Subscriber[String] { // TODO change to fs2 or similar
    def onSubscribe(s: Subscription): Unit = s.request(Long.MaxValue)
    def onNext(t: String): Unit = queue.add(t); ()
    def onError(t: Throwable): Unit = throw t
    def onComplete(): Unit = ()
  })
  LazyList.continually(queue.take)

def prompt[A: Decoder: SerialDescriptor](message: String)(using ScalaConversation): A =
  prompt(Prompt(message))

def promptMessage(message: String)(using ScalaConversation): String =
  promptMessage(Prompt(message))

def promptMessages(message: String)(using ScalaConversation): List[String] =
  promptMessages(Prompt(message))

def promptStreaming(message: String)(using ScalaConversation): LazyList[String] =
  promptStreaming(Prompt(message))

def images(prompt: Prompt, images: Images = DEFAULT_IMAGES, numberImages: Int = 1, size: String = "1024x1024")(using
    conversation: ScalaConversation
): ImagesGenerationResponse =
  conversation.images(images, prompt, numberImages, size).join()

def conversation[A](block: ScalaConversation ?=> A, id: ConversationId = ConversationId(randomUUID.toString)): A =
  block(using ScalaConversation(LocalVectorStore(DEFAULT_EMBEDDING), LogsMetric(), id))

private def fromJson[A: Decoder]: FromJson[A] = json => parse(json).flatMap(Decoder[A].decodeJson(_)).fold(throw _, identity)
