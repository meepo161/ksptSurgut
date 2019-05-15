package ru.avem.ksptamur.model.phase1;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment2ModelPhase1 {

    private final StringProperty UOutput;
    private final StringProperty UInput;
    private final StringProperty UDiff;
    private final StringProperty F;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();

    private final StringProperty groupHH;
    private final StringProperty groupBH;


    public Experiment2ModelPhase1() {
        UOutput = new SimpleStringProperty();
        UInput = new SimpleStringProperty();
        UDiff = new SimpleStringProperty();
        F = new SimpleStringProperty();
        result = new SimpleStringProperty();
        properties.addAll(Arrays.asList(UOutput, UInput, UDiff, F, result));

        groupBH = new SimpleStringProperty();
        groupHH = new SimpleStringProperty();
    }

    public String getUOutput() {
        return UOutput.get();
    }

    public StringProperty UOutputProperty() {
        return UOutput;
    }

    public void setUOutput(String UOutput) {
        this.UOutput.set(UOutput);
    }

    public String getUInput() {
        return UInput.get();
    }

    public StringProperty UInputProperty() {
        return UInput;
    }

    public void setUInput(String UInput) {
        this.UInput.set(UInput);
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
