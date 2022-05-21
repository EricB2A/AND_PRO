package com.example.blender

import androidx.recyclerview.widget.DiffUtil


class ConversationDiffCallback(
    private val oldList: List<Message>,
    private val newList: List<Message>
) :
    DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // TODO à completer une fois les modèles faits
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // TODO à completer une fois les modèles faits
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        return old == new
    }
}
