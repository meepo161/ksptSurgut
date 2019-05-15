package ru.avem.ksptamur.model.phase1;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment1ModelPhase1 {

    public static final double BREAK_IKAS = 1.0E9;
    private final StringProperty winding;
    private final StringProperty R;
    private final StringProperty temperature;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();

    public Experiment1ModelPhase1(String winding) {
        this.winding = new SimpleStringProperty(winding);
        R = new SimpleStringProperty("");
        temperature = new SimpleStringProperty("");
        result = new SimpleStringProperty("");
        properties.addAll(Arrays.asList(R, temperature, result));
    }

    public String getWinding() {
        return winding.get();
    }

    public StringProperty windingProperty() {
        return winding;
    }

    public void setWinding(String winding) {
        this.winding.set(winding);
    }

    public String getR() {
        return R.get();
    }

    public StringProperty RProperty() {
        return R;
    }

    public void setR(double R) {
        if (R == BREAK_IKAS || R == 2147483647) {
            this.R.set("Обрыв");
        } else {
            this.R.set(String.valueOf(R));
        }
    }

    public String getTemperature() {
        return temperature.get();
    }

    public StringProperty temperatureProperty() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature.set(temperature);
    }

    public String getResult() {
        return result.get();
    }

    public StringProperty resultProperty() {
        return result;
    }

    public void setResult(String result) {
        this.result.set(result);
    }

    public void clearProperties() {
        properties.forEach(stringProperty -> stringProperty.set(""));
    }
}
