package ru.avem.ksptamur.model.phase3;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment4ModelPhase3 {

    private final StringProperty groupHH;
    private final StringProperty groupBH;
    private final StringProperty result;
    private final StringProperty UBH;
    private final StringProperty UHH;
    private List<StringProperty> properties = new ArrayList<>();


    public Experiment4ModelPhase3() {
        groupBH = new SimpleStringProperty();
        groupHH = new SimpleStringProperty();
        result = new SimpleStringProperty();
        UBH = new SimpleStringProperty();
        UHH = new SimpleStringProperty();
        properties.addAll(Arrays.asList(groupBH, groupHH, UBH, UHH, result));
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

    public String getResult() {
        return result.get();
    }

    public StringProperty resultProperty() {
        return result;
    }

    public void setResult(String result) {
        this.result.set(result);
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

    public void setUHH(String UHH) {
        this.UHH.set(UHH);
    }

    public void clearProperties() {
        properties.forEach(stringProperty -> stringProperty.set(""));
    }
}