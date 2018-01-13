package com.pastew.isexam

import android.app.Application
import com.pastew.isexam.data.Subject
import com.pastew.isexam.data.Subjects


class SubjectsCreator(private val application: Application) {

    var subjects = Subjects()

    init {
        var currentSubject: Subject
        currentSubject = createExtendedSubjectInstance(name = "Wstęp do Programowania",
                questionCount = 30, idPrefix = 2000)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Ochrona Środowiska",
                questionCount = 20, firstQuestionId = 31, idPrefix = 3000)
        currentSubject.extend(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "BHP",
                questionCount = 7, firstQuestionId = 51, idPrefix = 4000)
        currentSubject = createExtendedSubjectInstance(name = "Architektury Komputerów",
                questionCount = 34, firstQuestionId = 58, idPrefix = 5000)
        currentSubject.extend(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Algorytmy",
                questionCount = 28, firstQuestionId = 92, idPrefix = 6000)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Statystyka",
                questionCount = 30, firstQuestionId = 120, idPrefix = 7000)
        currentSubject = createExtendedSubjectInstance(name = "Systemy Operacyjne",
                questionCount = 50, firstQuestionId = 150, idPrefix = 8000)
        currentSubject.extend(1)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Metaloznawstwo",
                questionCount = 29, firstQuestionId = 200, idPrefix = 9000)
        currentSubject.extend(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Programowanie Obiektowe",
                questionCount = 27, firstQuestionId = 229, idPrefix = 10000)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Multimedialne Techniki Internetowe",
                questionCount = 30, firstQuestionId = 256, idPrefix = 11000)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Mechanika Płynów", questionCount = 30,
                firstQuestionId = 286, idPrefix = 12000)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Mechanika Ciała", questionCount = 26,
                firstQuestionId = 316, idPrefix = 13000)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Ekonomika", questionCount = 21,
                firstQuestionId = 342, idPrefix = 14000)
        currentSubject = createExtendedSubjectInstance(name = "Metody Numeryczne", questionCount = 30,
                firstQuestionId = 363, idPrefix = 15000)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Sieci", questionCount = 33,
                firstQuestionId = 393, idPrefix = 16000)
        currentSubject.extend(1, 2, 3, 4, 5, 6, 7, 8, 9)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Bazy Danych", questionCount = 41,
                firstQuestionId = 426, idPrefix = 17000)
        currentSubject.extend(1, 2)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Java", questionCount = 30,
                firstQuestionId = 467, idPrefix = 18000)
        currentSubject.extend(1, 2, 3, 4, 5)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Grafika", questionCount = 27,
                firstQuestionId = 497, idPrefix = 19000)
        currentSubject.extend(1, 2)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Prawo Patentowe", questionCount = 27,
                firstQuestionId = 524, idPrefix = 20000)
        currentSubject = createExtendedSubjectInstance(name = "Podstawy Sztucznej Inteligencji", questionCount = 26,
                firstQuestionId = 551, idPrefix = 21000)
        currentSubject.extend(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Inżynieria Oprogramowania", questionCount = 29,
                firstQuestionId = 577, idPrefix = 22000)
        subjects.add(currentSubject)
        currentSubject.extend(1, 2)
        currentSubject = createExtendedSubjectInstance(name = "Inżynieria Internetu", questionCount = 30,
                firstQuestionId = 606, idPrefix = 23000)
        currentSubject.extend(1, 2, 3, 4, 5, 6)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Programowanie Równoległe", questionCount = 20,
                firstQuestionId = 636, idPrefix = 24000)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Metoda Elementów Skończonych", questionCount = 28,
                firstQuestionId = 656, idPrefix = 25000)
        currentSubject.extend(1)
        subjects.add(currentSubject)
        currentSubject = createExtendedSubjectInstance(name = "Wymiana Ciepła i Masy", questionCount = 28,
                firstQuestionId = 684, idPrefix = 26000)
        subjects.add(currentSubject)

        currentSubject = Subject("Całość", calculateQuestionArray())
        subjects.subjects.add(0, currentSubject)


    }

    private fun calculateQuestionArray(): IntArray {
        var list = mutableListOf<Int>()
        subjects.subjects.forEach {
            list.addAll(it.questionsIDs.asList())
        }
        println("LISTA $list")
        return list.toIntArray()
    }

    private fun createExtendedSubjectInstance(name: String, questionCount: Int, idPrefix: Int, firstQuestionId: Int = 1): ExtendedSubject {
        return ExtendedSubject.createInstance(name, questionCount, firstQuestionId, application, idPrefix)
    }


}
