package ru.avem.ksptamur.model.phase3;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment2ModelPhase3 {

    private final StringProperty UOutputAB;
    private final StringProperty UOutputBC;
    private final StringProperty UOutputCA;
    private final StringProperty UOutputAvr;
    private final StringProperty UInputAB;
    private final StringProperty UInputBC;
    private final StringProperty UInputCA;
    private final StringProperty UInputAvr;
    private final StringProperty UDiff;
    private final StringProperty F;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();

    private final StringProperty groupHH;
    private final StringProperty groupBH;


    public Experiment2ModelPhase3() {
        UOutputAB = new SimpleStringProperty();
        UOutputBC = new SimpleStringProperty();
        UOutputCA = new SimpleStringProperty();
        UOutputAvr = new SimpleStringProperty();
        UInputAB = new SimpleStringProperty();
        UInputBC = new SimpleStringProperty();
        UInputCA = new SimpleStringProperty();
        UInputAvr = new SimpleStringProperty();
        UDiff = new SimpleStringProperty();
        F = new SimpleStringProperty();
        result = new SimpleStringProperty();
        properties.addAll(Arrays.asList(UOutputAB, UOutputBC, UOutputCA, UOutputAvr, UInputAB, UInputBC, UInputCA,UInputAvr, UDiff, F, result));

        groupBH = new SimpleStringProperty();
        groupHH = new SimpleStringProperty();
    }

    public String getUOutputAB() {
        return UOutputAB.get();
    }

    public StringProperty UOutputABProperty() {
        return UOutputAB;
    }

    public void setUOutputAB(String UOutputAB) {
        this.UOutputAB.set(UOutputAB);
    }

    public String getUOutputBC() {
        return UOutputBC.get();
    }

    public StringProperty UOutputBCProperty() {
        return UOutputBC;
    }

    public void setUOutputBC(String UOutputBC) {
        this.UOutputBC.set(UOutputBC);
    }

    public String getUOutputCA() {
        return UOutputCA.get();
    }

    public StringProperty UOutputCAProperty() {
        return UOutputCA;
    }

    public void setUOutputCA(String UOutputCA) {
        this.UOutputCA.set(UOutputCA);
    }

    public String getUOutputAvr() {
        return UOutputAvr.get();
    }

    public StringProperty UOutputAvrProperty() {
        return UOutputAvr;
    }

    public void setUOutputAvr(String UOutputAvr) {
        this.UOutputAvr.set(UOutputAvr);
    }

    public String getUInputAB() {
        return UInputAB.get();
    }

    public StringProperty UInputABProperty() {
        return UInputAB;
    }

    public void setUInputAB(String UInputAB) {
        this.UInputAB.set(UInputAB);
    }

    public String getUInputBC() {
        return UInputBC.get();
    }

    public StringProperty UInputBCProperty() {
        return UInputBC;
    }

    public void setUInputBC(String UInputBC) {
        this.UInputBC.set(UInputBC);
    }

    public String getUInputCA() {
        return UInputCA.get();
    }

    public StringProperty UInputCAProperty() {
        return UInputCA;
    }

    public void setUInputCA(String UInputCA) {
        this.UInputCA.set(UInputCA);
    }

    public String getUInputAvr() {
        return UInputAvr.get();
    }

    public StringProperty UInputAvrProperty() {
        return UInputAvr;
    }

    public void setUInputAvr(String UInputAvr) {
        this.UInputAvr.set(UInputAvr);
    }

    public String getUDiff() {
        return UDiff.get();
    }

    public StringProperty UDiffProperty() {
        return UDiff;
    }

    public void setUDiff(String UDiff) {
        this.UDiff.set(UDiff);
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


    public String getGroupHH() {
        return groupHH.get();
    }

    public StringProperty groupHHProperty() {
        return groupHH;
    }

    public void setGroupHH(String groupHH) {
        this.groupHH.set(groupHH);
    }

    public String getGroupBH() {
        return groupBH.get();
    }

    public StringProperty groupBHProperty() {
        return groupBH;
    }

    public void setGroupBH(String groupBH) {
        this.groupBH.set(groupBH);
    }

}
