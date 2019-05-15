package ru.avem.ksptamur.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ResultModel {

    private final StringProperty dimension;
    private final StringProperty value;

    public ResultModel(String dimension, String value) {
        this.dimension = new SimpleStringProperty(dimension);
        this.value = new SimpleStringProperty(value);
    }

    public String getDimension() {
        return dimension.get();
    }

    public StringProperty dimensionProperty() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension.set(dimension);
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }
}
