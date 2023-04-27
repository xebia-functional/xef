package com.xebia.functional

import arrow.core.raise.NullableRaise
import arrow.core.raise.nullable
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.autoCloseable
import arrow.fx.coroutines.resourceScope
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import javax.sql.DataSource

suspend fun <A> DataSource.connection(block: suspend JDBCSyntax.() -> A): A =
  resourceScope {
    val conn = autoCloseable { connection }
    JDBCSyntax(conn, this).block()
  }

class JDBCSyntax(conn: Connection, resourceScope: ResourceScope) : ResourceScope by resourceScope, Connection by conn {

  suspend fun prepareStatement(
    sql: String,
    binders: (SqlPreparedStatement.() -> Unit)? = null
  ): PreparedStatement = autoCloseable {
    prepareStatement(sql)
      .apply { if (binders != null) SqlPreparedStatement(this).binders() }
  }

  suspend fun update(
    sql: String,
    binders: (SqlPreparedStatement.() -> Unit)? = null,
  ): Unit {
    val statement = prepareStatement(sql, binders)
    statement.executeUpdate()
  }

  suspend fun <A> queryOneOrNull(
    sql: String,
    binders: (SqlPreparedStatement.() -> Unit)? = null,
    mapper: NullableSqlCursor.() -> A
  ): A? {
    val statement = prepareStatement(sql, binders)
    val rs = autoCloseable { statement.executeQuery() }
    return if (rs.next()) nullable { mapper(NullableSqlCursor(rs, this)) }
    else null
  }

  suspend fun <A> queryAsList(
    sql: String,
    binders: (SqlPreparedStatement.() -> Unit)? = null,
    mapper: NullableSqlCursor.() -> A?
  ): List<A> {
    val statement = prepareStatement(sql, binders)
    println(statement.toString())
    val rs = autoCloseable { statement.executeQuery() }
    return buildList {
      while (rs.next()) {
        nullable { mapper(NullableSqlCursor(rs, this)) }?.let(::add)
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
    fun long(): Long = raise.ensureNotNull(resultSet.getLong(index++).takeUnless { resultSet.wasNull() })
    fun double(): Double = raise.ensureNotNull(resultSet.getDouble(index++).takeUnless { resultSet.wasNull() })
    fun nextRow(): Boolean = resultSet.next()
  }
}
