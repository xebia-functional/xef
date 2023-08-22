package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.conversation.llm.openai.*
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.conversation.{FromJson, JVMConversation}
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.tracing.*
import com.xebia.functional.xef.llm.models.images.*
import com.xebia.functional.xef.store.{ConversationId, LocalVectorStore, VectorStore}
import io.circe.Decoder
import io.circe.parser.parse
import org.reactivestreams.{Subscriber, Subscription}
import java.util
import java.util.UUID
import java.util.concurrent.LinkedBlockingQueue
import scala.jdk.CollectionConverters.*

class ScalaConversation(store: VectorStore, conversationId: Option[ConversationId], dispatcher: Dispatch[Event]) extends JVMConversation(store, conversationId.orNull, dispatcher)

def addContext(context: Array[String])(using conversation: ScalaConversation): Unit =
  conversation.addContextFromArray(context).join()

def prompt[A: Decoder: SerialDescriptor](
    prompt: Prompt,
    chat: ChatWithFunctions = OpenAI.FromEnvironment.DEFAULT_SERIALIZATION
)(using
    conversation: ScalaConversation
): A =
  val fromJson = new FromJson[A] {
    def fromJson(json: String): A =
      parse(json).flatMap(Decoder[A].decodeJson(_)).fold(throw _, identity)
  }
  conversation.prompt(chat, prompt, chat.chatFunction(SerialDescriptor[A].serialDescriptor), fromJson).join()

def promptMessage(
    prompt: Prompt,
    chat: Chat = OpenAI.FromEnvironment.DEFAULT_CHAT
)(using conversation: ScalaConversation): String =
  conversation.promptMessage(chat, prompt).join()

def promptMessages(
    prompt: Prompt,
    chat: Chat = OpenAI.FromEnvironment.DEFAULT_CHAT
)(using
    conversation: ScalaConversation
): List[String] =
  conversation.promptMessages(chat, prompt).join().asScala.toList

def promptStreaming(
    prompt: Prompt,
    chat: Chat = OpenAI.FromEnvironment.DEFAULT_CHAT
)(using
    conversation: ScalaConversation
): LazyList[String] =
  val publisher = conversation.promptStreamingToPublisher(chat, prompt)
  val queue = new LinkedBlockingQueue[String]()
  publisher.subscribe(new Subscriber[String] {
    // TODO change to fs2 or similar
    def onSubscribe(s: Subscription): Unit = s.request(Long.MaxValue)

    def onNext(t: String): Unit = queue.add(t); ()

    def onError(t: Throwable): Unit = throw t

    def onComplete(): Unit = ()
  })
  LazyList.continually(queue.take)

def images(
    prompt: Prompt,
    images: Images = OpenAI.FromEnvironment.DEFAULT_IMAGES,
    numberImages: Int = 1,
    size: String = "1024x1024"
)(using
    conversation: ScalaConversation
): ImagesGenerationResponse =
  conversation.images(images, prompt, numberImages, size).join()

def conversation[A](
    block: ScalaConversation ?=> A,
    conversationId: Option[ConversationId] = Some(ConversationId(UUID.randomUUID().toString)),
    dispatcher: Dispatch[Event]
): A = block(using ScalaConversation(LocalVectorStore(OpenAI.FromEnvironment.DEFAULT_EMBEDDING), conversationId, dispatcher))
