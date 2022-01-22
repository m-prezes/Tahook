package com.tahook;

import java.util.LinkedHashMap;

import org.json.simple.JSONObject;

public class Question {
    private String question;
    private String answer_a;
    private String answer_b;
    private String answer_c;
    private String answer_d;
    private int anwser_time;

    public Question(String question, String answer_a, String answer_b, String answer_c, String answer_d,
            int anwser_time) {
        this.question = question;
        this.answer_a = answer_a;
        this.answer_b = answer_b;
        this.answer_c = answer_c;
        this.answer_d = answer_d;
        this.anwser_time = anwser_time;
    }

    public Question() {
        this.question = null;
        this.answer_a = null;
        this.answer_b = null;
        this.answer_c = null;
        this.answer_d = null;
        this.anwser_time = 60;
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

    public int getAnwser_time() {
        return anwser_time;
    }

    public void setAnswer_time(int anwser_time) {
        this.anwser_time = anwser_time;
    }

    public String getWholeQuestion() {
        return this.question + " " + this.answer_a + " " + this.answer_b + " " +
                this.answer_c + " " + this.answer_d;
    }

    public String getJSON() {
        LinkedHashMap<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("question", this.question);
        response.put("anwser_a", this.answer_a);
        response.put("anwser_b", this.answer_b);
        response.put("anwser_c", this.answer_c);
        response.put("anwser_d", this.answer_d);
        response.put("anwser_time", this.anwser_time);
        String jsonString = new JSONObject(response).toString();

        return jsonString;
    }

}
