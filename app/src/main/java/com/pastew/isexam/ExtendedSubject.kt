package com.pastew.isexam

import android.app.Application
import android.content.Context
import com.pastew.isexam.data.Subject


class ExtendedSubject(name: String, questionsIDs: IntArray, private val idPrefix: Int) : Subject(name, questionsIDs) {

    var lastQuestionId: Int = questionsIDs[questionsIDs.lastIndex]

    fun extend(vararg ids: Int) {
        val list = questionsIDs.toMutableList()
        ids.forEach {
            list.add((it+idPrefix))
        }
        updateQuestionsIDs(list.toIntArray())

    }


    companion object {

        fun createInstance(name: String, questionCount: Int, firstQuestionId: Int = 1, application: Application, idPrefix: Int): ExtendedSubject {
            return ExtendedSubject(name, createArray(firstQuestionId, questionCount, application, name), idPrefix)
        }

        fun createArray(firstQuestionId: Int, questionCount: Int, application: Application, name: String): IntArray {
            val list = mutableListOf<Int>()
            val appContext = application.applicationContext
            for (i in (firstQuestionId..(questionCount + firstQuestionId - 1))) {
                if (checkIfQuestionExists(i, appContext)) {
                    list.add(i)
                } else {
                    println("QUESTION NUMBER $i DOESN'T EXISTS IN SUBJECT $name")
                }

            }

            return list.toIntArray()

        }

        private fun checkIfQuestionExists(questionNumber: Int, context: Context): Boolean {
            return context.resources.getIdentifier("img" + questionNumber + "_" + 'p', "drawable", context.packageName) != 0
        }

    }

}
