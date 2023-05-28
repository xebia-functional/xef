package com.xebia.functional.xef.treesitter

import com.sun.jna.Pointer
import com.sun.jna.Structure

class TreeCursor : Structure(), Structure.ByValue, Iterator<Node> {

    @JvmField val tree: Tree? = null
    @JvmField val id: Pointer? = null
    @JvmField val context = intArrayOf(0, 0)

    var init = false

    fun gotoFirstChild(): Boolean {
        val b = TreeSitter.INSTANCE.ts_tree_cursor_goto_first_child(this.pointer)
        init = true

        return b
    }

    fun gotoNextSibling() {
        TreeSitter.INSTANCE.ts_tree_cursor_goto_next_sibling(this.pointer)
    }

    override fun getFieldOrder(): List<String> {
        return listOf("tree", "id", "context")
    }

    override fun hasNext(): Boolean {
        if (!init) {
            return true
        }

        return !TreeSitter.INSTANCE.ts_node_next_sibling(currentNode).isNull
    }

    val currentNode: Node
        get() {
            return TreeSitter.INSTANCE.ts_tree_cursor_current_node(this.pointer)
        }

    val currentFieldName: String?
        get() {
            return TreeSitter.INSTANCE.ts_tree_cursor_current_field_name(this.pointer)
        }

    override fun next(): Node {
        if (!init) {
            gotoFirstChild()
        } else {
            gotoNextSibling()
        }

        return currentNode
    }
}
