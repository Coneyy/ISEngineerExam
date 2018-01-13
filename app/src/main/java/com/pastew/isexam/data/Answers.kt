package com.pastew.isexam.data

import com.pastew.isexam.IndexedAnswer
import java.util.*

class Answers(private val answers: ArrayList<IndexedAnswer>) {


    operator fun get(index: Int): Answer {
        return answers.find { it.index == index }?.answer ?: Answer.UNKNOWN
    }

    fun size(): Int {
        return answers.size
    }
}
