package ru.avem.ksptsurgut.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;

@DatabaseTable(tableName = "testItems")
public class TestItem {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String type;

    @DatabaseField
    private double ubh;

    @DatabaseField
    private double uhh;

    @DatabaseField
    private double p;

    @DatabaseField
    private double ixx;

    @DatabaseField
    private double ukz;

    @DatabaseField
    private double xxtime;

    @DatabaseField
    private double uinsulation;

    @DatabaseField
    private double umeger;

    @DatabaseField
    private double withMeger;  // 1.0 - true, 0.0 - false

    public TestItem() {
        // ORMLite needs a no-arg constructor
    }

    public TestItem(String type, double ubh, double uhh, double p, double ixx, double ukz, double xxtime, double uinsulation, double umeger) {
        this.type = type;
        this.ubh = ubh;
        this.uhh = uhh;
        this.p = p;
        this.ixx = ixx;
        this.ukz = ukz;
        this.xxtime = xxtime;
        this.uinsulation = uinsulation;
        this.umeger = umeger;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public double getUbh() {
        return ubh;
    }

    public void setUbh(double ubh) {
        this.ubh = ubh;
    }

    public double getUhh() {
        return uhh;
    }

    public void setUhh(double uhh) {
        this.uhh = uhh;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public double getIxx() {
        return ixx;
    }

    public void setIxx(double ixx) {
        this.ixx = ixx;
    }

    public double getUkz() {
        return ukz;
    }

    public void setUkz(double ukz) {
        this.ukz = ukz;
    }

    public double getXxtime() {
        return xxtime;
    }

    public void setXxtime(double xxtime) {
        this.xxtime = xxtime;
    }

    public double getUinsulation() {
        return uinsulation;
    }

    public void setUinsulation(double uinsulation) {
        this.uinsulation = uinsulation;
    }

    public double getUmeger() {
        return umeger;
    }

    public void setUmeger(double umeger) {
        this.umeger = umeger;
    }

    @Override
    public String toString() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestItem testItem = (TestItem) o;
        return id == testItem.id &&
                Double.compare(testItem.ubh, ubh) == 0 &&
                Double.compare(testItem.uhh, uhh) == 0 &&
                Double.compare(testItem.p, p) == 0 &&
                Double.compare(testItem.ixx, ixx) == 0 &&
                Double.compare(testItem.ukz, ukz) == 0 &&
                Double.compare(testItem.xxtime, xxtime) == 0 &&
                Double.compare(testItem.uinsulation, uinsulation) == 0 &&
                Double.compare(testItem.withMeger, withMeger) == 0 &&
                Objects.equals(type, testItem.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, ubh, uhh, p, ixx, ukz, xxtime, uinsulation, withMeger);
    }
}