package com.example.blender

class Discussion(_from: String, _text: String) {
    var received = false
    var content = _text
    var from = _from

    constructor(_from: String, _text: String, _received: Boolean) : this(_from, _text) {
        received = _received
    }
}
