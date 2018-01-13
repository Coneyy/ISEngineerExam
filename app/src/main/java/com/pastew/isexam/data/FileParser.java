package com.pastew.isexam.data;

import com.pastew.isexam.IndexedAnswer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileParser {

    public static Answers readAnswers(InputStream inputStream) throws IOException {
        ArrayList<IndexedAnswer> answers = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;

        while ((line = reader.readLine()) != null) {
            answers.add(getAnswer(line));
        }

        return new Answers(answers);
    }

    public static Subjects readSubjects(InputStream inputStream) throws IOException {
        Subjects subjects = new Subjects();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        String[] splitLine;

        while ((line = reader.readLine()) != null) {
            splitLine = line.split("-");
            String name = splitLine[0];

            if (splitLine.length == 3) {
                int firstQuestionId = Integer.parseInt(splitLine[1]);
                int lastQuestionId = Integer.parseInt(splitLine[2]);
                subjects.add(new Subject(name, firstQuestionId, lastQuestionId));
            } else {
                String[] questionsIDsString = splitLine[1].split(",");
                int[] questionsIDs = new int[questionsIDsString.length];
                for (int i = 0; i < questionsIDsString.length; ++i)
                    questionsIDs[i] = Integer.parseInt(questionsIDsString[i]);

                subjects.add(new Subject(name, questionsIDs));
            }
        }

        return subjects;
    }

    private static IndexedAnswer getAnswer(String line) {
        String[] split = line.split(":");
        int index = Integer.valueOf(split[0].replaceAll("\\p{C}", ""));
        char answerChar = line.charAt(line.length() - 1);

        switch (answerChar) {
            case 'A':
                return new IndexedAnswer(Answer.A, index);
            case 'B':
                return new IndexedAnswer(Answer.B, index);
            case 'C':
                return new IndexedAnswer(Answer.C, index);
            default:
                return new IndexedAnswer(Answer.UNKNOWN, index);
        }
    }

}
