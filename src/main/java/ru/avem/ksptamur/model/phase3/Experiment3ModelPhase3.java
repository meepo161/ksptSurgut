package ru.avem.ksptamur.model.phase3;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment3ModelPhase3 {

    private final StringProperty uOutputAB;
    private final StringProperty uOutputBC;
    private final StringProperty uOutputCA;
    private final StringProperty uOutputAvr;
    private final StringProperty uInputAB;
    private final StringProperty uInputBC;
    private final StringProperty uInputCA;
    private final StringProperty uInputAvr;
    private final StringProperty uDiff;
    private final StringProperty groupBH;
    private final StringProperty groupHH;
    private final StringProperty f;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();


    public Experiment3ModelPhase3() {
        uOutputAB = new SimpleStringProperty();
        uOutputBC = new SimpleStringProperty();
        uOutputCA = new SimpleStringProperty();
        uOutputAvr = new SimpleStringProperty();
        uInputAB = new SimpleStringProperty();
        uInputBC = new SimpleStringProperty();
        uInputCA = new SimpleStringProperty();
        uInputAvr = new SimpleStringProperty();
        uDiff = new SimpleStringProperty();
        groupBH = new SimpleStringProperty();
        groupHH = new SimpleStringProperty();
        f = new SimpleStringProperty();
        result = new SimpleStringProperty();
        properties.addAll(Arrays.asList(uOutputAB, uOutputBC, uOutputCA, uOutputAvr, uInputAB, uInputBC, uInputCA, uInputAvr, uDiff, f, result));
    }

    public String getuOutputAB() {
        return uOutputAB.get();
    }

    public StringProperty uOutputABProperty() {
        return uOutputAB;
    }

    public void setuOutputAB(String uOutputAB) {
        this.uOutputAB.set(uOutputAB);
    }

    public String getuOutputBC() {
        return uOutputBC.get();
    }

    public StringProperty uOutputBCProperty() {
        return uOutputBC;
    }

    public void setuOutputBC(String uOutputBC) {
        this.uOutputBC.set(uOutputBC);
    }

    public String getuOutputCA() {
        return uOutputCA.get();
    }

    public StringProperty uOutputCAProperty() {
        return uOutputCA;
    }

    public void setuOutputCA(String uOutputCA) {
        this.uOutputCA.set(uOutputCA);
    }

    public String getuOutputAvr() {
        return uOutputAvr.get();
    }

    public StringProperty uOutputAvrProperty() {
        return uOutputAvr;
    }

    public void setuOutputAvr(String uOutputAvr) {
        this.uOutputAvr.set(uOutputAvr);
    }

    public String getuInputAB() {
        return uInputAB.get();
    }

    public StringProperty uInputABProperty() {
        return uInputAB;
    }

    public void setuInputAB(String uInputAB) {
        this.uInputAB.set(uInputAB);
    }

    public String getuInputBC() {
        return uInputBC.get();
    }

    public StringProperty uInputBCProperty() {
        return uInputBC;
    }

    public void setuInputBC(String uInputBC) {
        this.uInputBC.set(uInputBC);
    }

    public String getuInputCA() {
        return uInputCA.get();
    }

    public StringProperty uInputCAProperty() {
        return uInputCA;
    }

    public void setuInputCA(String uInputCA) {
        this.uInputCA.set(uInputCA);
    }

    public String getuInputAvr() {
        return uInputAvr.get();
    }

    public StringProperty uInputAvrProperty() {
        return uInputAvr;
    }

    public void setuInputAvr(String uInputAvr) {
        this.uInputAvr.set(uInputAvr);
    }

    public String getuDiff() {
        return uDiff.get();
    }

    public StringProperty uDiffProperty() {
        return uDiff;
    }

    public void setuDiff(String uDiff) {
        this.uDiff.set(uDiff);
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

    public String getGroupHH() {
        return groupHH.get();
    }

    public StringProperty groupHHProperty() {
        return groupHH;
    }

    public void setGroupHH(String groupHH) {
        this.groupHH.set(groupHH);
    }

    public String getF() {
        return f.get();
    }

    public StringProperty fProperty() {
        return f;
    }

    public void setF(String f) {
        this.f.set(f);
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
