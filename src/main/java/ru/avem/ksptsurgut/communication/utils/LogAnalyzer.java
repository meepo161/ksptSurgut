package ru.avem.ksptsurgut.communication.utils;

import ru.avem.ksptsurgut.utils.Log;

public class LogAnalyzer {
    private final Class<LogAnalyzer> TAG = LogAnalyzer.class;

    private final String name;

    private int all;
    private int correct;

    public LogAnalyzer(String name) {
        this.name = name;
    }

    public void addWrite() {
        all++;
    }

    public void addSuccess() {
        correct++;
        float failure = all - correct;
        Log.d(TAG, String.format(
                "[%s] All: %d, Correct: %d, Failure: %.0f, Failure Percent: %.4f\n",
                name, all, correct, failure, failure / all));
    }
}
