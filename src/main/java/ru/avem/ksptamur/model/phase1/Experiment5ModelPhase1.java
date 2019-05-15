package ru.avem.ksptamur.model.phase1;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment5ModelPhase1 {

    private final StringProperty UBH;
    private final StringProperty UKZPercent;
    private final StringProperty UKZDiff;
    private final StringProperty I;
    private final StringProperty PP;
    private final StringProperty F;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();


    public Experiment5ModelPhase1() {
        UBH = new SimpleStringProperty();
        UKZPercent = new SimpleStringProperty();
        UKZDiff = new SimpleStringProperty();
        I = new SimpleStringProperty();
        PP = new SimpleStringProperty();
        F = new SimpleStringProperty();
        result = new SimpleStringProperty();
        properties.addAll(Arrays.asList(UBH, UKZPercent, UKZDiff, I, PP, F, result));
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

    public String getUKZPercent() {
        return UKZPercent.get();
    }

    public StringProperty UKZPercentProperty() {
        return UKZPercent;
    }

    public void setUKZPercent(String UKZPercent) {
        this.UKZPercent.set(UKZPercent);
    }

    public String getUKZDiff() {
        return UKZDiff.get();
    }

    public StringProperty UKZDiffProperty() {
        return UKZDiff;
    }

    public void setUKZDiff(String UKZDiff) {
        this.UKZDiff.set(UKZDiff);
    }

    public String getI() {
        return I.get();
    }

    public StringProperty IProperty() {
        return I;
    }

    public void setI(double I) {
        this.I.set(String.valueOf(I));
    }

    public void setPP(double pp) {
        this.PP.set(String.valueOf(pp));
    }

    public String getPP() {
        return PP.get();
    }

    public StringProperty PProperty() {
        return PP;
    }

    public void setPP(String PP) {
        this.PP.set(PP);
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

