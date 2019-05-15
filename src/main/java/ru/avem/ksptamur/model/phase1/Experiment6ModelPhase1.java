package ru.avem.ksptamur.model.phase1;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment6ModelPhase1 {

    private final StringProperty UBH;
    private final StringProperty I;
    private final StringProperty IPercent;
    private final StringProperty IDiff;
    private final StringProperty PP;
    private final StringProperty F;
    private final StringProperty COS;
    private final StringProperty Time;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();


    public Experiment6ModelPhase1() {
        UBH = new SimpleStringProperty();
        I = new SimpleStringProperty();
        IPercent = new SimpleStringProperty();
        IDiff = new SimpleStringProperty();
        PP = new SimpleStringProperty();
        COS = new SimpleStringProperty();
        Time = new SimpleStringProperty();
        F = new SimpleStringProperty();

        result = new SimpleStringProperty();
        properties.addAll(Arrays.asList(UBH, I, IPercent, IDiff, PP, COS, Time, F, result));
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


    public String getI() {
        return I.get();
    }

    public StringProperty IProperty() {
        return I;
    }

    public void setI(double I) {
        this.I.set(String.valueOf(I));
    }

    public String getIDiff() {
        return IDiff.get();
    }

    public StringProperty IDiffProperty() {
        return IDiff;
    }

    public void setIDiff(String IDiff) {
        this.IDiff.set(IDiff);
    }

    public String getIPercent() {
        return IPercent.get();
    }

    public StringProperty IPercentProperty() {
        return IPercent;
    }

    public void setIPercent(String IPercent) {
        this.IPercent.set(IPercent);
    }

    public void setPP(String pp) {
        this.PP.set(pp);
    }

    public String getPP() {
        return PP.get();
    }

    public StringProperty PProperty() {
        return PP;
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

    public String getCOS() {
        return COS.get();
    }

    public StringProperty COSProperty() {
        return COS;
    }

    public void setCOS(String COS) {
        this.COS.set(COS);
    }

    public String getTime() {
        return Time.get();
    }

    public StringProperty timeProperty() {
        return Time;
    }

    public void setTime(String T) {
        this.Time.set(String.valueOf(T));
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
