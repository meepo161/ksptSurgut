package ru.avem.ksptsurgut.model.phase3;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment5ModelPhase3 {

    private final StringProperty UBH;
    private final StringProperty IA;
    private final StringProperty IB;
    private final StringProperty IC;
    private final StringProperty IAPercent;
    private final StringProperty IBPercent;
    private final StringProperty ICPercent;
    private final StringProperty IADiff;
    private final StringProperty IBDiff;
    private final StringProperty ICDiff;
    private final StringProperty PP;
    private final StringProperty COS;
    private final StringProperty F;
    private final StringProperty Time;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();


    public Experiment5ModelPhase3() {
        UBH = new SimpleStringProperty();
        IA = new SimpleStringProperty();
        IB = new SimpleStringProperty();
        IC = new SimpleStringProperty();
        IAPercent = new SimpleStringProperty();
        IBPercent = new SimpleStringProperty();
        ICPercent = new SimpleStringProperty();
        IADiff = new SimpleStringProperty();
        IBDiff = new SimpleStringProperty();
        ICDiff = new SimpleStringProperty();
        PP = new SimpleStringProperty();
        COS = new SimpleStringProperty();
        F = new SimpleStringProperty();
        Time = new SimpleStringProperty();

        result = new SimpleStringProperty();
        properties.addAll(Arrays.asList(UBH, IA, IB, IC, IAPercent, IBPercent, ICPercent, IADiff, IBDiff, ICDiff, PP, COS, F, result));
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

    public String getIAPercent() {
        return IAPercent.get();
    }

    public StringProperty IAPercentProperty() {
        return IAPercent;
    }

    public void setIAPercent(String IAPercent) {
        this.IAPercent.set(IAPercent);
    }

    public String getIBPercent() {
        return IBPercent.get();
    }

    public StringProperty IBPercentProperty() {
        return IBPercent;
    }

    public void setIBPercent(String IBPercent) {
        this.IBPercent.set(IBPercent);
    }

    public String getICPercent() {
        return ICPercent.get();
    }

    public StringProperty ICPercentProperty() {
        return ICPercent;
    }

    public void setICPercent(String ICPercent) {
        this.ICPercent.set(ICPercent);
    }

    public String getIADiff() {
        return IADiff.get();
    }

    public StringProperty IADiffProperty() {
        return IADiff;
    }

    public void setIADiff(String IADiff) {
        this.IADiff.set(IADiff);
    }

    public String getIBDiff() {
        return IBDiff.get();
    }

    public StringProperty IBDiffProperty() {
        return IBDiff;
    }

    public void setIBDiff(String IBDiff) {
        this.IBDiff.set(IBDiff);
    }

    public String getICDiff() {
        return ICDiff.get();
    }

    public StringProperty ICDiffProperty() {
        return ICDiff;
    }

    public void setICDiff(String ICDiff) {
        this.ICDiff.set(ICDiff);
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
