package ru.avem.ksptamur.model.phase3;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment7ModelPhase3 {

    private final StringProperty UIN;
    private final StringProperty IBH;
    private final StringProperty F;
    private final StringProperty time;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();


    public Experiment7ModelPhase3() {
        UIN = new SimpleStringProperty();
        IBH = new SimpleStringProperty();
        F = new SimpleStringProperty();
        time = new SimpleStringProperty();
        result = new SimpleStringProperty();
        properties.addAll(Arrays.asList(UIN, IBH, F, time, result));
    }

    public String getUIN() {
        return UIN.get();
    }

    public StringProperty UINProperty() {
        return UIN;
    }

    public void setUIN(String UIN) {
        this.UIN.set(UIN);
    }

    public String getIBH() {
        return IBH.get();
    }

    public StringProperty IBHProperty() {
        return IBH;
    }

    public void setIBH(double IBH) {
        this.IBH.set(String.valueOf(IBH));
    }

    public String getF() {
        return F.get();
    }

    public StringProperty FProperty() {
        return F;
    }

    public void setF(String f) {
        this.F.set(String.valueOf(f));
    }

    public String getTime() {
        return time.get();
    }

    public StringProperty timeProperty() {
        return time;
    }

    public void setTime(String T) {
        this.time.set(String.valueOf(T));
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

