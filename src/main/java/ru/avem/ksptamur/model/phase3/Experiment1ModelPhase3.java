package ru.avem.ksptamur.model.phase3;

        import javafx.beans.property.SimpleStringProperty;
        import javafx.beans.property.StringProperty;

        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.List;

public class Experiment1ModelPhase3 {

    private final StringProperty winding;
    private final StringProperty R15;
    private final StringProperty R60;
    private final StringProperty coef;
    private final StringProperty ur;
    private final StringProperty time;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();

    public Experiment1ModelPhase3(String winding) {
        this.winding = new SimpleStringProperty(winding);
        R15 = new SimpleStringProperty("");
        R60 = new SimpleStringProperty("");
        coef = new SimpleStringProperty("");
        ur = new SimpleStringProperty("");
        time = new SimpleStringProperty("");
        result = new SimpleStringProperty("");
        properties.addAll(Arrays.asList(R15, R60, coef, ur, time, result));
    }

    public String getWinding() {
        return winding.get();
    }

    public StringProperty windingProperty() {
        return winding;
    }

    public void setWinding(String winding) {
        this.winding.set(winding);
    }

    public String getR15() {
        return R15.get();
    }

    public StringProperty r15Property() {
        return R15;
    }

    public void setR15(String r15) {
        this.R15.set(r15);
    }

    public String getR60() {
        return R60.get();
    }

    public StringProperty r60Property() {
        return R60;
    }

    public void setR60(String r60) {
        this.R60.set(r60);
    }

    public String getCoef() {
        return coef.get();
    }

    public StringProperty coefProperty() {
        return coef;
    }

    public void setCoef(String coef) {
        this.coef.set(coef);
    }

    public String getUr() {
        return ur.get();
    }

    public StringProperty urProperty() {
        return ur;
    }

    public void setUr(String ur) {
        this.ur.set(ur);
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
