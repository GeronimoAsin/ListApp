
package ar.edu.itba.listapp.ui

import java.util.Stack

class History {
    private val stack = Stack<String>()

    fun push(item: String) {
        if (stack.isEmpty() || stack.peek() != item) {
            stack.push(item)
        }
    }

    fun pop(): String? {
        return if (stack.isNotEmpty()) {
            stack.pop()
        } else {
            null
        }
    }

    fun peek(): String? {
        return if (stack.isNotEmpty()) {
            stack.peek()
        } else {
            null
        }
    }

    fun isEmpty(): Boolean {
        return stack.isEmpty()
    }

    fun clear() {
        stack.clear()
    }
}
