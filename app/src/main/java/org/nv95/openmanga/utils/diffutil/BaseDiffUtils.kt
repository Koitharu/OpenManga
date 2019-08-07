package org.nv95.openmanga.utils.diffutil

import androidx.recyclerview.widget.DiffUtil


abstract class BaseDiffUtils(
        private val oldList: List<Any>,
        private val newList: List<Any>
) : DiffUtil.Callback() {

    final override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val item1 = oldList[oldItemPosition]
        val item2 = newList[newItemPosition]
        return areItemsTheSame(item1, item2)
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    abstract fun areContentsTheSame(item1: Any, item2: Any): Boolean

    open fun areItemsTheSame(item1: Any, item2: Any): Boolean {
        return item1.javaClass == item2.javaClass
    }

    final override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val item1 = oldList[oldItemPosition]
        val item2 = newList[newItemPosition]
        return if (areContentsTheSame(item1, item2)) true else item1 == item2
    }
}