package ru.avem.ksptamur.model;

import ru.avem.ksptamur.db.model.Account;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.db.model.TestItem;
import ru.avem.ksptamur.model.phase3.*;

public class MainModel {

    public static final int EXPERIMENT1_BOTH = 0;
    public static final int EXPERIMENT1_BH = 1;
    public static final int EXPERIMENT1_HH = 2;


    public static final int EXPERIMENT2_ONLY = 3;
    public static final int EXPERIMENT2_WITH_NOLOAD_AND_PHASEMETER = 4;
    public static final int EXPERIMENT2_WITH_PHASEMETER = 5;
    public static final int EXPERIMENT2_WITH_NOLOAD = 6;


    public static final int EXPERIMENT7_BOTH = 0;
    public static final int EXPERIMENT7_BH = 1;
    public static final int EXPERIMENT7_HH = 2;


    private static MainModel instance = new MainModel();

    private Account firstTester = new Account("ADMIN", "ADMIN", "ADMIN", "ADMIN", "ADMIN");
    private Account secondTester = new Account("ADMIN", "ADMIN", "ADMIN", "ADMIN", "ADMIN");

    private boolean isNeedRefresh = true;
    private Protocol currentProtocol;

    private Protocol intermediateProtocol;

    private int experiment0Choice;
    private int experiment1Choice;
    private int experiment2Choice;
    private int experiment7Choice;

    private Experiment0ModelPhase3 experiment0ModelPhase3BH = new Experiment0ModelPhase3("BH и К.");
    private Experiment0ModelPhase3 experiment0ModelPhase3HH = new Experiment0ModelPhase3("HH и К.");
    private Experiment0ModelPhase3 experiment0ModelPhase3BHHH = new Experiment0ModelPhase3("ВН и HH");
    private Experiment1ModelPhase3 experiment1ModelPhase3BH = new Experiment1ModelPhase3("BH");
    private Experiment1ModelPhase3 experiment1ModelPhase3HH = new Experiment1ModelPhase3("HH");
    private Experiment2ModelPhase3 experiment2ModelPhase3 = new Experiment2ModelPhase3();
    private Experiment3ModelPhase3 experiment3ModelPhase3 = new Experiment3ModelPhase3();
    private Experiment4ModelPhase3 experiment4ModelPhase3 = new Experiment4ModelPhase3();
    private Experiment5ModelPhase3 experiment5ModelPhase3 = new Experiment5ModelPhase3();
    private Experiment6ModelPhase3 experiment6ModelPhase3 = new Experiment6ModelPhase3();
    private Experiment7ModelPhase3 experiment7ModelPhase3BH = new Experiment7ModelPhase3("BH");
    private Experiment7ModelPhase3 experiment7ModelPhase3HH = new Experiment7ModelPhase3("HH");


    private MainModel() {
    }

    public static MainModel getInstance() {
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

    public int getExperiment0Choice() {
        return experiment0Choice;
    }

    public void setExperiment0Choice(int experiment0Choice) {
        this.experiment0Choice = experiment0Choice;
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

    public Experiment0ModelPhase3 getExperiment0ModelPhase3BH() {
        return experiment0ModelPhase3BH;
    }

    public void setExperiment0ModelPhase3BH(Experiment0ModelPhase3 experiment0ModelPhase3BH) {
        this.experiment0ModelPhase3BH = experiment0ModelPhase3BH;
    }

    public Experiment0ModelPhase3 getExperiment0ModelPhase3HH() {
        return experiment0ModelPhase3HH;
    }

    public void setExperiment0ModelPhase3HH(Experiment0ModelPhase3 experiment0ModelPhase3HH) {
        this.experiment0ModelPhase3HH = experiment0ModelPhase3HH;
    }

    public Experiment0ModelPhase3 getExperiment0ModelPhase3BHHH() {
        return experiment0ModelPhase3BHHH;
    }

    public void setExperiment0ModelPhase3BHHH(Experiment0ModelPhase3 experiment0ModelPhase3BHHH) {
        this.experiment0ModelPhase3BHHH = experiment0ModelPhase3BHHH;
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

    public Experiment2ModelPhase3 getExperiment2ModelPhase3() {
        return experiment2ModelPhase3;
    }

    public void setExperiment2ModelPhase3(Experiment2ModelPhase3 experiment2ModelPhase3) {
        this.experiment2ModelPhase3 = experiment2ModelPhase3;
    }

    public Experiment3ModelPhase3 getExperiment3ModelPhase3() {
        return experiment3ModelPhase3;
    }

    public void setExperiment3ModelPhase3(Experiment3ModelPhase3 experiment3ModelPhase3) {
        this.experiment3ModelPhase3 = experiment3ModelPhase3;
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
