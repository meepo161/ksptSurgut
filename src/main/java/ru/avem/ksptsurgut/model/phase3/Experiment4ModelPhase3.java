package ru.avem.ksptsurgut.model.phase3;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment4ModelPhase3 {

    private final StringProperty UBH1;
    private final StringProperty UBH2;
    private final StringProperty UBH3;
    private final StringProperty UKZPercent;
    private final StringProperty UKZDiff;
    private final StringProperty IA;
    private final StringProperty IB;
    private final StringProperty IC;
    private final StringProperty PP;
    private final StringProperty F;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();


    public Experiment4ModelPhase3() {
        UBH1 = new SimpleStringProperty();
        UBH2 = new SimpleStringProperty();
        UBH3 = new SimpleStringProperty();
        UKZPercent = new SimpleStringProperty();
        UKZDiff = new SimpleStringProperty();
        IA = new SimpleStringProperty();
        IB = new SimpleStringProperty();
        IC = new SimpleStringProperty();
        PP = new SimpleStringProperty();
        F = new SimpleStringProperty();
        result = new SimpleStringProperty();
        properties.addAll(Arrays.asList(UBH1, UBH2, UBH3, UKZPercent, UKZDiff, IA, IB, IC, PP, F, result));
    }

    public String getUBH1() {
        return UBH1.get();
    }

    public StringProperty UBH1Property() {
        return UBH1;
    }

    public void setUBH1(String UBH1) {
        this.UBH1.set(UBH1);
    }

    public String getUBH2() {
        return UBH2.get();
    }

    public StringProperty UBH2Property() {
        return UBH2;
    }

    public void setUBH2(String UBH2) {
        this.UBH2.set(UBH2);
    }

    public String getUBH3() {
        return UBH3.get();
    }

    public StringProperty UBH3Property() {
        return UBH3;
    }

    public void setUBH3(String UBH3) {
        this.UBH3.set(UBH3);
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

    public String getIA() {
        return IA.get();
    }

    public StringProperty IAProperty() {
        return IA;
    }

    public void setIA(String IA) {
        this.IA.set(IA);
    }

    public String getIB() {
        return IB.get();
    }

    public StringProperty IBProperty() {
        return IB;
    }

    public void setIB(String IB) {
        this.IB.set(IB);
    }

    public String getIC() {
        return IC.get();
    }

    public StringProperty ICProperty() {
        return IC;
    }

    public void setIC(String IC) {
        this.IC.set(IC);
    }

    public String getPP() {
        return PP.get();
    }

    public StringProperty PPProperty() {
        return PP;
    }

    public void setPP(String PP) {
        this.PP.set(PP);
    }

    public String getF() {
        return F.get();
    }

    public StringProperty fProperty() {
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

