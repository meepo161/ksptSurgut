package ru.avem.ksptamur.model;

import ru.avem.ksptamur.db.model.Account;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.db.model.TestItem;
import ru.avem.ksptamur.model.phase1.*;
import ru.avem.ksptamur.model.phase3.*;

public class MainModel {
    public static final int EXPERIMENT0_BH = 1;
    public static final int EXPERIMENT0_HH = 2;
    public static final int EXPERIMENT0_BHHH = 3;
    public static final int EXPERIMENT0_BH_HH = 4;
    public static final int EXPERIMENT0_BHHH_BH = 5;
    public static final int EXPERIMENT0_BHHH_HH = 6;
    public static final int EXPERIMENT0_ALL = 7;

    public static final int EXPERIMENT1_BOTH = 0;
    public static final int EXPERIMENT1_BH = 1;
    public static final int EXPERIMENT1_HH = 2;


    public static final int EXPERIMENT2_ONLY = 3;
    public static final int EXPERIMENT2_WITH_NOLOAD_AND_PHASEMETER = 4;
    public static final int EXPERIMENT2_WITH_PHASEMETER = 5;
    public static final int EXPERIMENT2_WITH_NOLOAD = 6;


    public static final int EXPERIMENT8_BOTH = 0;
    public static final int EXPERIMENT8_BH = 1;
    public static final int EXPERIMENT8_HH = 2;


    private static MainModel instance = new MainModel();

    private Account firstTester = new Account("ADMIN", "ADMIN", "ADMIN", "ADMIN", "ADMIN");
    private Account secondTester = new Account("ADMIN", "ADMIN", "ADMIN", "ADMIN", "ADMIN");

    private boolean isNeedRefresh = true;
    private Protocol currentProtocol;

    private Protocol intermediateProtocol;

    private int experiment0Choice;
    private int experiment1Choice;
    private int experiment2Choice;
    private int experiment8Choice;

    private Experiment1ModelPhase1 experiment1ModelPhase1BH = new Experiment1ModelPhase1("BH");
    private Experiment1ModelPhase1 experiment1ModelPhase1HH = new Experiment1ModelPhase1("HH");
    private Experiment2ModelPhase1 experiment2ModelPhase1 = new Experiment2ModelPhase1();
    private Experiment3ModelPhase1 experiment3ModelPhase1 = new Experiment3ModelPhase1();
    private Experiment4ModelPhase1 experiment4ModelPhase1 = new Experiment4ModelPhase1();
    private Experiment5ModelPhase1 experiment5ModelPhase1 = new Experiment5ModelPhase1();
    private Experiment6ModelPhase1 experiment6ModelPhase1 = new Experiment6ModelPhase1();
    private Experiment7ModelPhase1 experiment7ModelPhase1 = new Experiment7ModelPhase1();
    private Experiment8ModelPhase1 experiment8ModelPhase1BH = new Experiment8ModelPhase1("BH");
    private Experiment8ModelPhase1 experiment8ModelPhase1HH = new Experiment8ModelPhase1("HH");


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
    private Experiment7ModelPhase3 experiment7ModelPhase3 = new Experiment7ModelPhase3();
    private Experiment8ModelPhase3 experiment8ModelPhase3BH = new Experiment8ModelPhase3("BH");
    private Experiment8ModelPhase3 experiment8ModelPhase3HH = new Experiment8ModelPhase3("HH");


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

    public Experiment0ModelPhase3 getExperiment0ModelPhase3BH() {
        return experiment0ModelPhase3BH;
    }

    public Experiment0ModelPhase3 getExperiment0ModelPhase3HH() {
        return experiment0ModelPhase3HH;
    }

    public Experiment0ModelPhase3 getExperiment0ModelPhase3BHHH() {
        return experiment0ModelPhase3BHHH;
    }

    public int getExperiment0Choice() {
        return experiment0Choice;
    }

    public void setExperiment0Choice(int experiment0Choice) {
        this.experiment0Choice = experiment0Choice;
    }

    public Experiment1ModelPhase3 getExperiment1ModelPhase3BH() {
        return experiment1ModelPhase3BH;
    }

    public Experiment1ModelPhase3 getExperiment1ModelPhase3HH() {
        return experiment1ModelPhase3HH;
    }

    public int getExperiment1Choice() {
        return experiment1Choice;
    }

    public void setExperiment1Choice(int experiment1Choice) {
        this.experiment1Choice = experiment1Choice;
    }

    public Experiment2ModelPhase3 getExperiment2ModelPhase3() {
        return experiment2ModelPhase3;
    }

    public int getExperiment2Choice() {
        return experiment2Choice;
    }

    public void setExperiment2Choice(int experiment2Choice) {
        this.experiment2Choice = experiment2Choice;
    }

    public Experiment3ModelPhase3 getExperiment3ModelPhase3() {
        return experiment3ModelPhase3;
    }

    public Experiment4ModelPhase3 getExperiment4ModelPhase3() {
        return experiment4ModelPhase3;
    }

    public Experiment5ModelPhase3 getExperiment5ModelPhase3() {
        return experiment5ModelPhase3;
    }

    public Experiment6ModelPhase3 getExperiment6ModelPhase3() {
        return experiment6ModelPhase3;
    }

    public Experiment7ModelPhase3 getExperiment7ModelPhase3() {
        return experiment7ModelPhase3;
    }

    public Experiment8ModelPhase3 getExperiment8ModelPhase3BH() {
        return experiment8ModelPhase3BH;
    }

    public Experiment8ModelPhase3 getExperiment8ModelPhase3HH() {
        return experiment8ModelPhase3HH;
    }

    public int getExperiment8Choice() {
        return experiment8Choice;
    }

    public void setExperiment8Choice(int experiment8Choice) {
        this.experiment8Choice = experiment8Choice;
    }

    public Experiment1ModelPhase1 getExperiment1ModelPhase1BH() {
        return experiment1ModelPhase1BH;
    }

    public Experiment1ModelPhase1 getExperiment1ModelPhase1HH() {
        return experiment1ModelPhase1HH;
    }

    public Experiment2ModelPhase1 getExperiment2ModelPhase1() {
        return experiment2ModelPhase1;
    }

    public Experiment3ModelPhase1 getExperiment3ModelPhase1() {
        return experiment3ModelPhase1;
    }

    public Experiment4ModelPhase1 getExperiment4ModelPhase1() {
        return experiment4ModelPhase1;
    }

    public Experiment5ModelPhase1 getExperiment5ModelPhase1() {
        return experiment5ModelPhase1;
    }

    public Experiment6ModelPhase1 getExperiment6ModelPhase1() {
        return experiment6ModelPhase1;
    }

    public Experiment7ModelPhase1 getExperiment7ModelPhase1() {
        return experiment7ModelPhase1;
    }

    public Experiment8ModelPhase1 getExperiment8ModelPhase1BH() {
        return experiment8ModelPhase1BH;
    }

    public Experiment8ModelPhase1 getExperiment8ModelPhase1HH() {
        return experiment8ModelPhase1HH;
    }
}
