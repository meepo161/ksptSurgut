package ru.avem.ksptamur.model.phase1;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment3ModelPhase1 {

    private final StringProperty UBH;
    private final StringProperty UHH;
    private final StringProperty F;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();


    public Experiment3ModelPhase1() {
        UBH = new SimpleStringProperty();
        UHH = new SimpleStringProperty();
        F = new SimpleStringProperty();
        result = new SimpleStringProperty();
        properties.addAll(Arrays.asList(UBH, UHH, F, result));
    }

    public String getUBH() {
        return UBH.get();
    }

    public StringProperty UBHProperty() {
        return UBH;
    }

    public void setUBH(String UBH) {
        this.UBH.set(UBH);
    }

    public String getUHH() {
        return UHH.get();
    }

    public StringProperty UHHProperty() {
        return UHH;
    }

    public void setUHH(String  UHH) {
        this.UHH.set(UHH);
    }

    public String getF() {
        return F.get();
    }

    public StringProperty FProperty() {
        return F;
    }

    public void setF(String f) {
        this.F.set(f);
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
