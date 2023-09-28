package com.xebia.functional.xef.store.postgresql

import arrow.core.raise.NullableRaise
import arrow.core.raise.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import javax.sql.DataSource

inline fun <A> DataSource.connection(block: JDBCSyntax.() -> A): A =
  connection.use { conn ->
    JDBCSyntax(conn).block()
  }

class JDBCSyntax(val conn: Connection) : Connection by conn {

  inline fun prepareStatement(
    sql: String,
    binders: (SqlPreparedStatement.() -> Unit)
  ): PreparedStatement =
    conn.prepareStatement(sql).apply { SqlPreparedStatement(this).binders() }

  fun update(sql: String): Unit =
    prepareStatement(sql).use { statement ->
      statement.executeUpdate()
    }

  inline fun update(
    sql: String,
    binders: (SqlPreparedStatement.() -> Unit)
  ): Unit =
    prepareStatement(sql, binders).use { statement ->
      statement.executeUpdate()
    }

  inline fun <A> queryOneOrNull(
    sql: String,
    mapper: NullableSqlCursor.() -> A
  ): A? =
    prepareStatement(sql).use { statement ->
      statement.executeQuery().use { rs ->
        if (rs.next()) nullable { mapper(NullableSqlCursor(rs, this)) } else null
      }
    }

  inline fun <A> queryOneOrNull(
    sql: String,
    binders: (SqlPreparedStatement.() -> Unit),
    mapper: NullableSqlCursor.() -> A
  ): A? =
    prepareStatement(sql, binders).use { statement ->
      statement.executeQuery().use { rs ->
        if (rs.next()) nullable { mapper(NullableSqlCursor(rs, this)) } else null
      }
    }

  inline fun <A> queryAsList(
    sql: String,
    mapper: NullableSqlCursor.() -> A?
  ): List<A> =
    prepareStatement(sql).use { statement ->
      statement.executeQuery().use { rs ->
        buildList {
          while (rs.next()) {
            nullable { mapper(NullableSqlCursor(rs, this)) }?.let(::add)
          }
        }
      }
    }

  inline fun <A> queryAsList(
    sql: String,
    binders: (SqlPreparedStatement.() -> Unit),
    mapper: NullableSqlCursor.() -> A?
  ): List<A> =
    prepareStatement(sql, binders).use { statement ->
      statement.executeQuery().use { rs ->
        buildList {
          while (rs.next()) {
            nullable { mapper(NullableSqlCursor(rs, this)) }?.let(::add)
          }
        }
      }
    }

  class SqlPreparedStatement(private val preparedStatement: PreparedStatement) {
    private var index: Int = 1

    fun bind(short: Short?): Unit = bind(short?.toLong())
    fun bind(byte: Byte?): Unit = bind(byte?.toLong())
    fun bind(int: Int?): Unit = bind(int?.toLong())
    fun bind(char: Char?): Unit = bind(char?.toString())

    fun bind(bytes: ByteArray?): Unit =
      if (bytes == null) preparedStatement.setNull(index++, Types.BLOB)
      else preparedStatement.setBytes(index++, bytes)

    fun bind(long: Long?): Unit =
      if (long == null) preparedStatement.setNull(index++, Types.INTEGER)
      else preparedStatement.setLong(index++, long)

    fun bind(double: Double?): Unit =
      if (double == null) preparedStatement.setNull(index++, Types.REAL)
      else preparedStatement.setDouble(index++, double)

    fun bind(string: String?): Unit =
      if (string == null) preparedStatement.setNull(index++, Types.VARCHAR)
      else preparedStatement.setString(index++, string)

    inline fun <reified T>bind(value: T?): Unit =
      bind(Json.encodeToString(serializer(), value))
  }

  class SqlCursor(private val resultSet: ResultSet) {
    private var index: Int = 1
    fun int(): Int? = long()?.toInt()
    fun string(): String? = resultSet.getString(index++)
    fun bytes(): ByteArray? = resultSet.getBytes(index++)
    fun long(): Long? = resultSet.getLong(index++).takeUnless { resultSet.wasNull() }
    fun double(): Double? = resultSet.getDouble(index++).takeUnless { resultSet.wasNull() }
    fun nextRow(): Boolean = resultSet.next()
  }

  class NullableSqlCursor(private val resultSet: ResultSet, private val raise: NullableRaise) {
    private var index: Int = 1
    fun int(): Int = long().toInt()
    fun string(): String = raise.ensureNotNull(resultSet.getString(index++))
    fun bytes(): ByteArray = raise.ensureNotNull(resultSet.getBytes(index++))
    fun long(): Long =
      raise.ensureNotNull(resultSet.getLong(index++).takeUnless { resultSet.wasNull() })
    inline fun <reified T> serializable(): T = Json.decodeFromString(serializer(), string())

    fun double(): Double =
      raise.ensureNotNull(resultSet.getDouble(index++).takeUnless { resultSet.wasNull() })

    fun nextRow(): Boolean = resultSet.next()
  }
}
