package ru.avem.ksptsurgut.model;

import ru.avem.ksptsurgut.db.model.Account;
import ru.avem.ksptsurgut.db.model.Protocol;
import ru.avem.ksptsurgut.db.model.TestItem;
import ru.avem.ksptsurgut.model.phase3.*;

public class ExperimentValuesModel {

    public static final int EXPERIMENT2_BOTH = 0;
    public static final int EXPERIMENT2_BH = 1;
    public static final int EXPERIMENT2_HH = 2;

    private int experiment2ChoiceMask;

    public static final int EXPERIMENT7_BOTH = 0;
    public static final int EXPERIMENT7_BH = 1;
    public static final int EXPERIMENT7_HH = 2;


    private static ExperimentValuesModel instance = new ExperimentValuesModel();

    private Account firstTester = new Account("ADMIN", "ADMIN", "ADMIN", "ADMIN", "ADMIN");
    private Account secondTester = new Account("ADMIN", "ADMIN", "ADMIN", "ADMIN", "ADMIN");

    private boolean isNeedRefresh = true;
    private Protocol currentProtocol;

    private Protocol intermediateProtocol;

    private int experiment1Choice;
    private int experiment2Choice;
    private int experiment7Choice;

    private Experiment1ModelPhase3 experiment1ModelPhase3BH = new Experiment1ModelPhase3("BH и К.");
    private Experiment1ModelPhase3 experiment1ModelPhase3HH = new Experiment1ModelPhase3("HH и К.");
    private Experiment1ModelPhase3 experiment1ModelPhase3BHHH = new Experiment1ModelPhase3("ВН и HH");
    private Experiment2ModelPhase3 experiment2ModelPhase3HH = new Experiment2ModelPhase3("HH");
    private Experiment2ModelPhase3 experiment2ModelPhase3BH = new Experiment2ModelPhase3("BH");
    private Experiment2ModelPhase3 experiment2ModelPhase3BH2 = new Experiment2ModelPhase3("BH2");
    private Experiment2ModelPhase3 experiment2ModelPhase3BH3 = new Experiment2ModelPhase3("BH3");
    private Experiment2ModelPhase3 experiment2ModelPhase3BH4 = new Experiment2ModelPhase3("BH4");
    private Experiment2ModelPhase3 experiment2ModelPhase3BH5 = new Experiment2ModelPhase3("BH5");
    private Experiment3ModelPhase3 experiment3ModelPhase3 = new Experiment3ModelPhase3("BH");
    private Experiment3ModelPhase3 experiment3ModelPhase3BH2 = new Experiment3ModelPhase3("BH2");
    private Experiment3ModelPhase3 experiment3ModelPhase3BH3 = new Experiment3ModelPhase3("BH3");
    private Experiment3ModelPhase3 experiment3ModelPhase3BH4 = new Experiment3ModelPhase3("BH4");
    private Experiment3ModelPhase3 experiment3ModelPhase3BH5 = new Experiment3ModelPhase3("BH5");
    private Experiment4ModelPhase3 experiment4ModelPhase3 = new Experiment4ModelPhase3();
    private Experiment5ModelPhase3 experiment5ModelPhase3 = new Experiment5ModelPhase3();
    private Experiment6ModelPhase3 experiment6ModelPhase3 = new Experiment6ModelPhase3();
    private Experiment7ModelPhase3 experiment7ModelPhase3BH = new Experiment7ModelPhase3("BH");
    private Experiment7ModelPhase3 experiment7ModelPhase3HH = new Experiment7ModelPhase3("HH");


    private ExperimentValuesModel() {
    }

    public static ExperimentValuesModel getInstance() {
        return instance;
    }

    public void setTesters(Account tester1, Account tester2) {
        this.firstTester = tester1;
        this.secondTester = tester2;
    }

    public boolean isNeedRefresh() {
        return isNeedRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        isNeedRefresh = needRefresh;
    }

    public void createNewProtocol(String serialNumber, TestItem selectedTestItem) {
        currentProtocol = new Protocol(serialNumber, selectedTestItem, firstTester, secondTester, System.currentTimeMillis());
    }

    public Protocol getCurrentProtocol() {
        return currentProtocol;
    }

    public void setCurrentProtocol(Protocol currentProtocol) {
        this.currentProtocol = currentProtocol;
    }

    public void setIntermediateProtocol(Protocol intermediateProtocol) {
        this.intermediateProtocol = intermediateProtocol;
    }

    public void applyIntermediateProtocol() {
        currentProtocol = intermediateProtocol;
        intermediateProtocol = null;
    }

    public int getExperiment1Choice() {
        return experiment1Choice;
    }

    public void setExperiment1Choice(int experiment1Choice) {
        this.experiment1Choice = experiment1Choice;
    }

    public int getExperiment2Choice() {
        return experiment2Choice;
    }

    public void setExperiment2Choice(int experiment2Choice) {
        this.experiment2Choice = experiment2Choice;
    }

    public int getExperiment7Choice() {
        return experiment7Choice;
    }

    public void setExperiment7Choice(int experiment7Choice) {
        this.experiment7Choice = experiment7Choice;
    }

    public Experiment1ModelPhase3 getExperiment1ModelPhase3BH() {
        return experiment1ModelPhase3BH;
    }

    public void setExperiment1ModelPhase3BH(Experiment1ModelPhase3 experiment1ModelPhase3BH) {
        this.experiment1ModelPhase3BH = experiment1ModelPhase3BH;
    }

    public Experiment1ModelPhase3 getExperiment1ModelPhase3HH() {
        return experiment1ModelPhase3HH;
    }

    public void setExperiment1ModelPhase3HH(Experiment1ModelPhase3 experiment1ModelPhase3HH) {
        this.experiment1ModelPhase3HH = experiment1ModelPhase3HH;
    }

    public Experiment1ModelPhase3 getExperiment1ModelPhase3BHHH() {
        return experiment1ModelPhase3BHHH;
    }

    public void setExperiment1ModelPhase3BHHH(Experiment1ModelPhase3 experiment1ModelPhase3BHHH) {
        this.experiment1ModelPhase3BHHH = experiment1ModelPhase3BHHH;
    }

    public Experiment2ModelPhase3 getExperiment2ModelPhase3BH() {
        return experiment2ModelPhase3BH;
    }

    public void setExperiment2ModelPhase3BH(Experiment2ModelPhase3 experiment2ModelPhase3BH) {
        this.experiment2ModelPhase3BH = experiment2ModelPhase3BH;
    }

    public Experiment2ModelPhase3 getExperiment2ModelPhase3HH() {
        return experiment2ModelPhase3HH;
    }

    public void setExperiment2ModelPhase3HH(Experiment2ModelPhase3 experiment2ModelPhase3HH) {
        this.experiment2ModelPhase3HH = experiment2ModelPhase3HH;
    }

    public Experiment2ModelPhase3 getExperiment2ModelPhase3BH2() {
        return experiment2ModelPhase3BH2;
    }

    public void setExperiment2ModelPhase3BH2(Experiment2ModelPhase3 experiment2ModelPhase3BH2) {
        this.experiment2ModelPhase3BH2 = experiment2ModelPhase3BH2;
    }

    public Experiment2ModelPhase3 getExperiment2ModelPhase3BH3() {
        return experiment2ModelPhase3BH3;
    }

    public void setExperiment2ModelPhase3BH3(Experiment2ModelPhase3 experiment2ModelPhase3BH3) {
        this.experiment2ModelPhase3BH3 = experiment2ModelPhase3BH3;
    }

    public Experiment2ModelPhase3 getExperiment2ModelPhase3BH4() {
        return experiment2ModelPhase3BH4;
    }

    public void setExperiment2ModelPhase3BH4(Experiment2ModelPhase3 experiment2ModelPhase3BH4) {
        this.experiment2ModelPhase3BH4 = experiment2ModelPhase3BH4;
    }

    public Experiment2ModelPhase3 getExperiment2ModelPhase3BH5() {
        return experiment2ModelPhase3BH5;
    }

    public void setExperiment2ModelPhase3BH5(Experiment2ModelPhase3 experiment2ModelPhase3BH5) {
        this.experiment2ModelPhase3BH5 = experiment2ModelPhase3BH5;
    }

    public Experiment3ModelPhase3 getExperiment3ModelPhase3() {
        return experiment3ModelPhase3;
    }

    public void setExperiment3ModelPhase3(Experiment3ModelPhase3 experiment3ModelPhase3) {
        this.experiment3ModelPhase3 = experiment3ModelPhase3;
    }

    public Experiment3ModelPhase3 getExperiment3ModelPhase3BH2() {
        return experiment3ModelPhase3BH2;
    }

    public void setExperiment3ModelPhase3BH2(Experiment3ModelPhase3 experiment3ModelPhase3BH2) {
        this.experiment3ModelPhase3BH2 = experiment3ModelPhase3BH2;
    }

    public Experiment3ModelPhase3 getExperiment3ModelPhase3BH3() {
        return experiment3ModelPhase3BH3;
    }

    public void setExperiment3ModelPhase3BH3(Experiment3ModelPhase3 experiment3ModelPhase3BH3) {
        this.experiment3ModelPhase3BH3 = experiment3ModelPhase3BH3;
    }

    public Experiment3ModelPhase3 getExperiment3ModelPhase3BH4() {
        return experiment3ModelPhase3BH4;
    }

    public void setExperiment3ModelPhase3BH4(Experiment3ModelPhase3 experiment3ModelPhase3BH4) {
        this.experiment3ModelPhase3BH4 = experiment3ModelPhase3BH4;
    }

    public Experiment3ModelPhase3 getExperiment3ModelPhase3BH5() {
        return experiment3ModelPhase3BH5;
    }

    public void setExperiment3ModelPhase3BH5(Experiment3ModelPhase3 experiment3ModelPhase3BH5) {
        this.experiment3ModelPhase3BH5 = experiment3ModelPhase3BH5;
    }

    public Experiment4ModelPhase3 getExperiment4ModelPhase3() {
        return experiment4ModelPhase3;
    }

    public void setExperiment4ModelPhase3(Experiment4ModelPhase3 experiment4ModelPhase3) {
        this.experiment4ModelPhase3 = experiment4ModelPhase3;
    }

    public Experiment5ModelPhase3 getExperiment5ModelPhase3() {
        return experiment5ModelPhase3;
    }

    public void setExperiment5ModelPhase3(Experiment5ModelPhase3 experiment5ModelPhase3) {
        this.experiment5ModelPhase3 = experiment5ModelPhase3;
    }

    public Experiment6ModelPhase3 getExperiment6ModelPhase3() {
        return experiment6ModelPhase3;
    }

    public void setExperiment6ModelPhase3(Experiment6ModelPhase3 experiment6ModelPhase3) {
        this.experiment6ModelPhase3 = experiment6ModelPhase3;
    }

    public Experiment7ModelPhase3 getExperiment7ModelPhase3BH() {
        return experiment7ModelPhase3BH;
    }

    public void setExperiment7ModelPhase3BH(Experiment7ModelPhase3 experiment7ModelPhase3BH) {
        this.experiment7ModelPhase3BH = experiment7ModelPhase3BH;
    }

    public Experiment7ModelPhase3 getExperiment7ModelPhase3HH() {
        return experiment7ModelPhase3HH;
    }

    public void setExperiment7ModelPhase3HH(Experiment7ModelPhase3 experiment7ModelPhase3HH) {
        this.experiment7ModelPhase3HH = experiment7ModelPhase3HH;
    }
}
