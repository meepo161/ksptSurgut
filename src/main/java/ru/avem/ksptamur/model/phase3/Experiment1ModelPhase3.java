package ru.avem.ksptamur.model.phase3;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.avem.ksptamur.utils.Utils.formatRealNumber;

public class Experiment1ModelPhase3 {

    private static final double BREAK_IKAS = 1.0E9;

    private final StringProperty winding;
    private final StringProperty AB;
    private final StringProperty BC;
    private final StringProperty AC;
    private final StringProperty temperature;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();

    public Experiment1ModelPhase3(String winding) {
        this.winding = new SimpleStringProperty(winding);
        AB = new SimpleStringProperty("");
        BC = new SimpleStringProperty("");
        AC = new SimpleStringProperty("");
        temperature = new SimpleStringProperty("");
        result = new SimpleStringProperty("");
        properties.addAll(Arrays.asList(AB, BC, AC, temperature, result));
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

    public String getAB() {
        return AB.get();
    }

    public StringProperty ABProperty() {
        return AB;
    }

    public void setAB(double AB) {
        if (AB == BREAK_IKAS) {
            this.AB.set("Обрыв");
        } else {
            this.AB.set(formatRealNumber(AB));
        }
    }

    public String getBC() {
        return BC.get();
    }

    public StringProperty BCProperty() {
        return BC;
    }

    public void setBC(double BC) {
        if (BC == BREAK_IKAS) {
            this.BC.set("Обрыв");
        } else {
            this.BC.set(formatRealNumber(BC));
        }
    }

    public String getAC() {
        return AC.get();
    }

    public StringProperty ACProperty() {
        return AC;
    }

    public void setAC(double AC) {
        if (AC == BREAK_IKAS) {
            this.AC.set("Обрыв");
        } else {
            this.AC.set(formatRealNumber(AC));
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
