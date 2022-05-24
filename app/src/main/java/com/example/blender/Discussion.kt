package com.example.blender

class Discussion(_from: String, _text: String) {
    var received = false
    var content = _text
    var from = _from

    constructor(_from: String, _text: String, _received: Boolean) : this(_from, _text) {
        received = _received
    }

    override fun equals(other: Any?): Boolean {
        if(other !is Discussion) return false
        return (received == other.received) && (content == other.content) && (from == other.from)
    }
}
