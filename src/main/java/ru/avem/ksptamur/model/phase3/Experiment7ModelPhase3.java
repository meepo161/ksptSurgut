package ru.avem.ksptamur.model.phase3;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment7ModelPhase3 {

    private final StringProperty type;
    private final StringProperty UGiven;
    private final StringProperty UAVEM;
    private final StringProperty IBH;
    private final StringProperty time;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();

    public Experiment7ModelPhase3(String type) {
        this.type = new SimpleStringProperty(type);
        this.UGiven = new SimpleStringProperty("");
        this.UAVEM = new SimpleStringProperty("");
        this.IBH = new SimpleStringProperty("");
        this.time = new SimpleStringProperty("");
        this.result = new SimpleStringProperty("");
        properties.addAll(Arrays.asList(UGiven, UAVEM, IBH, time, result));
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getTime() {
        return time.get();
    }

    public StringProperty timeProperty() {
        return time;
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public String getUGiven() {
        return UGiven.get();
    }

    public StringProperty UGivenProperty() {
        return UGiven;
    }

    public void setUGiven(String UGiven) {
        this.UGiven.set(UGiven);
    }

    public String getUAVEM() {
        return UAVEM.get();
    }

    public StringProperty UAVEMProperty() {
        return UAVEM;
    }

    public void setUAVEM(String UAVEM) {
        this.UAVEM.set(UAVEM);
    }

    public String getIBH() {
        return IBH.get();
    }

    public StringProperty IBHProperty() {
        return IBH;
    }

    public void setIBH(String IBH) {
        this.IBH.set(IBH);
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
