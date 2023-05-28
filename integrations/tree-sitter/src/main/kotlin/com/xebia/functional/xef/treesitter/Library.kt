package com.xebia.functional.xef.treesitter

import com.sun.jna.*
import com.sun.jna.Structure.ByValue
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

class Language : Structure, Structure.ByReference {
    @JvmField var version: Int = 0
    @JvmField var symbol_count: Int = 0

    constructor() : super()
    constructor(pointer: Pointer) : super(pointer)

    fun symbolName(symbol: Short): String {
        return TreeSitter.INSTANCE.ts_language_symbol_name(this, symbol)
    }

    override fun getFieldOrder() = listOf("version", "symbol_count")
}

interface AnyLangLibrary : Library {
    fun language(): Language

    companion object {
        private val langLibraries : ConcurrentHashMap<String, AnyLangLibrary> = ConcurrentHashMap()
        fun loadLanguage(language: String): AnyLangLibrary {
            return langLibraries.computeIfAbsent(language) {
                Native.load(language, AnyLangLibrary::class.java)
            }
        }
    }
}

/**
 * Represents `TSNode`. Note, that since `TSNode` is a structure, functions that return a [Node]
 * such as [Tree.rootNode] will always return a (potentially) empty node structure rather than
 * `null`. In the C API, the function [TreeSitter.ts_node_is_null] is needed to check before
 * retrieving more information, e.g. by using [TreeSitter.ts_node_string].
 *
 * For convenience and extra safety, calls to the properties such as [string] will therefore
 * internally check for [isNull] before further interacting with a node.
 */
class Node : Structure(), ByValue, Iterable<Node> {
    @JvmField var context = intArrayOf(0, 0, 0, 0)
    @JvmField var id: Pointer? = null
    @JvmField var tree: Tree? = null

    val string: String?
        get() {
            return if (!isNull) {
                TreeSitter.INSTANCE.ts_node_string(this)
            } else {
                null
            }
        }

    val type: String?
        get() {
            return if (!isNull) {
                TreeSitter.INSTANCE.ts_node_type(this)
            } else {
                null
            }
        }

    val childCount: Int
        get() {
            return if (!isNull) {
                TreeSitter.INSTANCE.ts_node_child_count(this)
            } else {
                0
            }
        }

    val namedChildCount: Int
        get() {
            return if (!isNull) {
                TreeSitter.INSTANCE.ts_node_named_child_count(this)
            } else {
                0
            }
        }

    val startByte: Int
        get() {
            return if (!isNull) {
                TreeSitter.INSTANCE.ts_node_start_byte(this)
            } else {
                0
            }
        }

    val endByte: Int
        get() {
            return if (!isNull) {
                TreeSitter.INSTANCE.ts_node_end_byte(this)
            } else {
                0
            }
        }

    val startPoint: Point
        get() {
            return if (!isNull) {
                TreeSitter.INSTANCE.ts_node_start_point(this)
            } else {
                Point()
            }
        }

    val endPoint: Point
        get() {
            return if (!isNull) {
                TreeSitter.INSTANCE.ts_node_end_point(this)
            } else {
                Point()
            }
        }

    val isNamed: Boolean
        get() {
            return TreeSitter.INSTANCE.ts_node_is_named(this)
        }

    val isNull: Boolean
        get() {
            // instead of calling ts_node_is_null we avoid the extra JNA round-trip and directly
            // check, whether the id field is null (which is exactly what ts_node_is_null does)
            return id == null
        }

    val nextNamedSibling: Node
        get() {
            return if (!isNull) {
                TreeSitter.INSTANCE.ts_node_next_named_sibling(this)
            } else {
                Node()
            }
        }

    public override fun getFieldOrder(): List<String> {
        return listOf("context", "id", "tree")
    }

    fun child(index: Int): Node {
        return if (!isNull) {
            return TreeSitter.INSTANCE.ts_node_child(this, index)
        } else {
            Node()
        }
    }

    fun namedChild(index: Int): Node {
        return if (!isNull) {
            return TreeSitter.INSTANCE.ts_node_named_child(this, index)
        } else {
            Node()
        }
    }

    fun childByFieldName(fieldName: String): Node {
        return if (!isNull) {
            TreeSitter.INSTANCE.ts_node_child_by_field_name(this, fieldName, fieldName.length)
        } else {
            Node()
        }
    }

    fun newCursor(): TreeCursor? {
        return if (!isNull) {
            val cursor = TreeSitter.INSTANCE.ts_tree_cursor_new(this)
            cursor
        } else {
            null
        }
    }

    override fun iterator(): TreeCursor {
        return newCursor() ?: TreeCursor()
    }
}

open class Length : Structure(), Structure.ByValue {
    @JvmField var bytes: Int = 0
    @JvmField var extent: Point = Point()

    override fun getFieldOrder() = listOf("bytes", "extent")
}

open class Point : Structure(), Structure.ByValue {
    @JvmField var row: Int = 0
    @JvmField var column: Int = 0

    override fun getFieldOrder() = listOf("row", "column")
}

class Logger : Structure(), Structure.ByValue {
    @JvmField var payload: Pointer? = null
    @JvmField var log: LogCallback? = null

    override fun getFieldOrder() = listOf("payload", "log")
}

interface LogCallback : Callback {
    fun log(payload: Pointer?, type: Int, msg: String)
}

class InputEdit : Structure(), Structure.ByReference {
    @JvmField var start_byte: Int = 0
    @JvmField var old_end_byte: Int = 0
    @JvmField var new_end_byte: Int = 0
    @JvmField var start_point: Point = Point()
    @JvmField var old_end_point: Point = Point()
    @JvmField var new_end_point: Point = Point()

    override fun getFieldOrder() =
        listOf(
            "start_byte",
            "old_end_byte",
            "new_end_byte",
            "start_point",
            "old_end_point",
            "new_end_point"
        )
}

class Tree : PointerType() {
    val language: Language
        get() {
            return TreeSitter.INSTANCE.ts_tree_language(this)
        }

    val rootNode: Node
        get() {
            return TreeSitter.INSTANCE.ts_tree_root_node(this)
        }

    fun edit(input: InputEdit) {
        return TreeSitter.INSTANCE.ts_tree_edit(this, input)
    }
}

open class Parser : PointerType(TreeSitter.INSTANCE.ts_parser_new()), Closeable {

    var language: Language?
        set(language) {
            if (language != null) {
                TreeSitter.INSTANCE.ts_parser_set_language(this, language)
            }
        }
        get(): Language? {
            return TreeSitter.INSTANCE.ts_parser_language(this)
        }

    fun parseString(oldTree: Tree?, string: String): Tree {
        return TreeSitter.INSTANCE.ts_parser_parse_string(
            this,
            oldTree,
            string.toByteArray(),
            string.length
        )
    }

    override fun close() {
        TreeSitter.INSTANCE.ts_parser_delete(this)
    }
}

interface TreeSitter : Library {
    /**
     * Creates a new `TSParser`. Note, this intentionally returns a [Pointer] instead of [Parser]
     * because we execute this function in the constructor of [Parser].
     */
    fun ts_parser_new(): Pointer
    fun ts_parser_set_language(self: Parser, language: Language)
    fun ts_parser_language(self: Parser): Language
    fun ts_parser_set_logger(self: Parser, logger: Logger)

    fun ts_parser_delete(parser: Parser)
    fun ts_parser_parse_string(self: Parser, oldTree: Tree?, string: ByteArray, length: Int): Tree
    fun ts_tree_root_node(self: Tree): Node
    fun ts_tree_edit(self: Tree, input: InputEdit)

    fun ts_node_new(tree: Tree?, subtree: Pointer?, position: Structure, alias: Int): Node
    fun ts_tree_language(self: Tree): Language
    fun ts_node_start_byte(self: Node): Int
    fun ts_node_end_byte(self: Node): Int
    fun ts_node_start_point(self: Node): Point
    fun ts_node_end_point(self: Node): Point
    fun ts_node_string(self: Node): String
    fun ts_node_type(self: Node): String
    fun ts_node_child_count(self: Node): Int
    fun ts_node_named_child_count(self: Node): Int
    fun ts_node_named_child(self: Node, childIndex: Int): Node
    fun ts_node_child(self: Node, childIndex: Int): Node
    fun ts_node_next_sibling(self: Node): Node
    fun ts_node_is_named(self: Node): Boolean
    fun ts_node_is_null(node: Node): Boolean
    fun ts_node_child_by_field_name(self: Node, fieldName: String, fieldNameLength: Int): Node
    fun ts_node_next_named_sibling(self: Node): Node

    fun ts_language_symbol_name(language: Language, symbol: Short): String

    fun ts_tree_cursor_new(node: Node): TreeCursor
    fun ts_tree_cursor_goto_first_child(self: Pointer): Boolean
    fun ts_tree_cursor_goto_next_sibling(self: Pointer)
    fun ts_tree_cursor_goto_parent(self: Pointer)
    fun ts_tree_cursor_current_field_name(self: Pointer): String?
    fun ts_tree_cursor_current_node(self: Pointer): Node

    companion object {
        val INSTANCE = Native.load("tree-sitter", TreeSitter::class.java) as TreeSitter
    }
}

/**
 * A convenience function to quickly retrieve a (named) child of this node by the field name.
 *
 * Usage:
 * ```
 * var node = /* ... */
 * var child = "field" of node
 * ```
 */
infix fun String.of(node: Node): Node {
    return node.childByFieldName(this)
}

/**
 * A convenience function to quickly retrieve a child of this node by its index.
 *
 * Usage:
 * ```
 * var node = /* ... */
 * var child = 0 of node
 * ```
 */
infix fun Int.of(node: Node): Node {
    return node.child(this)
}

/**
 * A convenience function to quickly retrieve a named child of this node by its index.
 *
 * Usage:
 * ```
 * var node = /* ... */
 * var child = 0 ofNamed node
 * ```
 */
infix fun Int.ofNamed(node: Node): Node {
    return node.namedChild(this)
}
