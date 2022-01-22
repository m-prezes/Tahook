package com.tahook;

import java.util.LinkedHashMap;

import org.json.simple.JSONObject;

public class Question {
    private String question;
    private String answer_a;
    private String answer_b;
    private String answer_c;
    private String answer_d;
    private String correctAnswer;
    private int answer_time;

    public Question(String question, String answer_a, String answer_b, String answer_c, String answer_d,
            int answer_time, String correctAnswer) {
        this.question = question;
        this.answer_a = answer_a;
        this.answer_b = answer_b;
        this.answer_c = answer_c;
        this.answer_d = answer_d;
        this.answer_time = answer_time;
        this.correctAnswer = correctAnswer;
    }

    public Question() {
        this.question = null;
        this.answer_a = null;
        this.answer_b = null;
        this.answer_c = null;
        this.answer_d = null;
        this.correctAnswer = null;
        this.answer_time = 60;
    };

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer_a() {
        return answer_a;
    }

    public void setAnswer_a(String answer_a) {
        this.answer_a = answer_a;
    }

    public String getAnswer_b() {
        return answer_b;
    }

    public void setAnswer_b(String answer_b) {
        this.answer_b = answer_b;
    }

    public String getAnswer_c() {
        return answer_c;
    }

    public void setAnswer_c(String answer_c) {
        this.answer_c = answer_c;
    }

    public String getAnswer_d() {
        return answer_d;
    }

    public void setAnswer_d(String answer_d) {
        this.answer_d = answer_d;
    }

    public int getAnswer_time() {return answer_time;}

    public void setAnswer_time(int anwser_time) {
        this.answer_time = anwser_time;
    }

    public String getCorrectAnswer() {return correctAnswer;}

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getWholeQuestion() {
        return this.question + " " + this.answer_a + " " + this.answer_b + " " +
                this.answer_c + " " + this.answer_d;
    }

    public String getJSON() {
        LinkedHashMap<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("question", this.question);
        response.put("answer_a", this.answer_a);
        response.put("answer_b", this.answer_b);
        response.put("answer_c", this.answer_c);
        response.put("answer_d", this.answer_d);
        response.put("correctAnswer", this.correctAnswer);
        response.put("answer_time", this.answer_time * 1000);
        String jsonString = new JSONObject(response).toString();

        return jsonString;
    }

}
