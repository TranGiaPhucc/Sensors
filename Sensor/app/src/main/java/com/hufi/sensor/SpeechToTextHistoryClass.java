package com.hufi.sensor;

public class SpeechToTextHistoryClass {
    int id;
    String date;
    String text;

    public SpeechToTextHistoryClass(String date, String text) {
        this.date = date;
        this.text = text;
    }

    @Override
    public String toString() {
        return "SpeechToTextHistoryClass{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", text='" + text + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
