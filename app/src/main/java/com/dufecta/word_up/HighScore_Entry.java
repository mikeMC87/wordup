package com.dufecta.word_up;

public class HighScore_Entry {

    private String name;
    private int score;

    public void setHighScore(String in_name, int in_score) {
        name = in_name;
        score = in_score;
    }

    public String getHighScoreName() {
        return name;
    }

    public int getHighScoreValue() {
        return score;
    }
}
