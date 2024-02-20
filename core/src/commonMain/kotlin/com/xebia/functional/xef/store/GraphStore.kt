package com.xebia.functional.xef.store

// Existing imports remain unchanged
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Duration

@Serializable
sealed class GraphResponse<out A> {
  abstract val value: A

  @Serializable
  data class StringValue(override val value: String) : GraphResponse<String>()

  @Serializable
  data class IntegerValue(override val value: Int) : GraphResponse<Int>()

  @Serializable
  data class FloatValue(override val value: Float) : GraphResponse<Float>()

  @Serializable
  data class BooleanValue(override val value: Boolean) : GraphResponse<Boolean>()

  @Serializable
  data class Point(val x: Int, val y: Int, val crs: String) : GraphResponse<Point>() {
    override val value: Point = this
  }

  @Serializable
  data class DateISOValue(override val value: String) : GraphResponse<String>()

  @Serializable
  data class DateTimeISOValue(override val value: String) : GraphResponse<String>()

  @Serializable
  data class DurationValue(override val value: Duration) : GraphResponse<Duration>()

  @JvmInline
  @Serializable
  value class FileUri(val uri: String)

  @JvmInline
  @Serializable
  value class NodeName(val id: String)

  sealed interface PropertyContainer

  @Serializable
  data class Node<out Value>(
    val name: NodeName,
    val label: String,
    val payload: Value,
    val files: List<FileUri> = emptyList()
  ) : PropertyContainer, GraphResponse<Node<Value>>() {
    override val value: Node<Value> = this
  }

  @Serializable
  data class Relationship<out Type>(
    val from: NodeName,
    val type: Type,
    val to: NodeName
  ) : PropertyContainer, GraphResponse<Relationship<Type>>() {
    override val value: Relationship<Type> = this
  }

  @Serializable
  data class Path(
    val start: NodeName,
    val elements: List<PropertyContainer>
  ) : GraphResponse<Path>() {
    override val value: Path = this
  }
}

// Models access to a graph via Cypher queries
interface GraphStore {

  /**
   * Executes a Cypher query on the graph and returns the result.
   *
   * @param query The Cypher query to execute.
   * @return The result of the query execution.
   */
  fun <A> executeQuery(query: String): GraphResponse<A>

}
