package ru.avem.ksptsurgut.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import javax.xml.bind.annotation.XmlRootElement;
import java.text.SimpleDateFormat;
import java.util.Objects;

@XmlRootElement
@DatabaseTable(tableName = "protocols")
public class Protocol {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String e1WindingBH = "";
    @DatabaseField
    private String e1UBH = "";
    @DatabaseField
    private String e1R15BH = "";
    @DatabaseField
    private String e1R60BH = "";
    @DatabaseField
    private String e1CoefBH = "";
    @DatabaseField
    private String e1ResultBH = "";

    @DatabaseField
    private String e1WindingHH = "";
    @DatabaseField
    private String e1UHH = "";
    @DatabaseField
    private String e1R15HH = "";
    @DatabaseField
    private String e1R60HH = "";
    @DatabaseField
    private String e1CoefHH = "";
    @DatabaseField
    private String e1ResultHH = "";

    @DatabaseField
    private String e2WindingBH = "";
    @DatabaseField
    private String e2ABBH = "";
    @DatabaseField
    private String e2BCBH = "";
    @DatabaseField
    private String e2CABH = "";
    @DatabaseField
    private String e2TBH = "";
    @DatabaseField
    private String e2ResultBH = "";

    @DatabaseField
    private String e2WindingHH = "";
    @DatabaseField
    private String e2ABHH = "";
    @DatabaseField
    private String e2BCHH = "";
    @DatabaseField
    private String e2CAHH = "";
    @DatabaseField
    private String e2THH = "";
    @DatabaseField
    private String e2ResultHH = "";

    @DatabaseField
    private String e3UInputAB = "";
    @DatabaseField
    private String e3UInputBC = "";
    @DatabaseField
    private String e3UInputCA = "";
    @DatabaseField
    private String e3UInputAvr = "";
    @DatabaseField
    private String e3UOutputAB = "";
    @DatabaseField
    private String e3UOutputBC = "";
    @DatabaseField
    private String e3UOutputCA = "";
    @DatabaseField
    private String e3UOutputAvr = "";
    @DatabaseField
    private String e3DiffU = "";
    @DatabaseField
    private String e3WindingBH = "";
    @DatabaseField
    private String e3WindingHH = "";
    @DatabaseField
    private String e3F = "";
    @DatabaseField
    private String e3Result = "";

    @DatabaseField
    private String e4UKZVA = "";
    @DatabaseField
    private String e4UKZVB = "";
    @DatabaseField
    private String e4UKZVC = "";
    @DatabaseField
    private String e4UKZPercent = "";
    @DatabaseField
    private String e4IA = "";
    @DatabaseField
    private String e4IB = "";
    @DatabaseField
    private String e4IC = "";
    @DatabaseField
    private String e4Pp = "";
    @DatabaseField
    private String e4F = "";
    @DatabaseField
    private String e4Result = "";

    @DatabaseField
    private String e5UBH = "";
    @DatabaseField
    private String e5IA = "";
    @DatabaseField
    private String e5IB = "";
    @DatabaseField
    private String e5IC = "";
    @DatabaseField
    private String e5IAPercent = "";
    @DatabaseField
    private String e5IBPercent = "";
    @DatabaseField
    private String e5ICPercent = "";
    @DatabaseField
    private String e5Pp = "";
    @DatabaseField
    private String e5F = "";
    @DatabaseField
    private String e5Result = "";

    @DatabaseField
    private String e6UInput = "";
    @DatabaseField
    private String e6IBH = "";
    @DatabaseField
    private String e6F = "";
    @DatabaseField
    private String e6Time = "";
    @DatabaseField
    private String e6Result = "";

    @DatabaseField
    private String serialNumber;
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
    private double umeger;
    @DatabaseField
    private String position1;
    @DatabaseField
    private String position1Number;
    @DatabaseField
    private String position1FullName;
    @DatabaseField
    private String position2;
    @DatabaseField
    private String position2Number;
    @DatabaseField
    private String position2FullName;
    @DatabaseField
    private long millis = System.currentTimeMillis();
    @DatabaseField
    private String date;
    @DatabaseField
    private String time;

    public Protocol() {
        // ORMLite and XML binder need a no-arg constructor
    }

    public Protocol(String serialNumber, ru.avem.ksptsurgut.db.model.TestItem selectedTestItem, ru.avem.ksptsurgut.db.model.Account firstTester, ru.avem.ksptsurgut.db.model.Account secondTester, long millis) {
        this.serialNumber = serialNumber;
        setObject(selectedTestItem);
        this.position1 = firstTester.getPosition();
        this.position1Number = firstTester.getNumber();
        this.position1FullName = firstTester.getFullName();
        this.position2 = secondTester.getPosition();
        this.position2Number = secondTester.getNumber();
        this.position2FullName = secondTester.getFullName();
        this.millis = millis;
        this.date = new SimpleDateFormat("dd.MM.yy").format(millis);
        this.time = new SimpleDateFormat("HH:mm:ss").format(millis);

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public ru.avem.ksptsurgut.db.model.TestItem getObject() {
        return new ru.avem.ksptsurgut.db.model.TestItem(type, ubh,
                uhh, p, xxtime, umeger);
    }

    public void setObject(ru.avem.ksptsurgut.db.model.TestItem object) {
        type = object.getType();
        ubh = object.getUbh();
        uhh = object.getUhh();
        p = object.getP();
        xxtime = object.getXxtime();
        umeger = object.getUmeger();
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
        this.date = new SimpleDateFormat("dd.MM.yy").format(millis);
    }

    public String getE1WindingBH() {
        return e1WindingBH;
    }

    public void setE1WindingBH(String e1WindingBH) {
        this.e1WindingBH = e1WindingBH;
    }

    public String getE1UBH() {
        return e1UBH;
    }

    public void setE1UBH(String e1UBH) {
        this.e1UBH = e1UBH;
    }

    public String getE1R15BH() {
        return e1R15BH;
    }

    public void setE1R15BH(String e1R15BH) {
        this.e1R15BH = e1R15BH;
    }

    public String getE1R60BH() {
        return e1R60BH;
    }

    public void setE1R60BH(String e1R60BH) {
        this.e1R60BH = e1R60BH;
    }

    public String getE1CoefBH() {
        return e1CoefBH;
    }

    public void setE1CoefBH(String e1CoefBH) {
        this.e1CoefBH = e1CoefBH;
    }

    public String getE1ResultBH() {
        return e1ResultBH;
    }

    public void setE1ResultBH(String e1ResultBH) {
        this.e1ResultBH = e1ResultBH;
    }

    public String getE1WindingHH() {
        return e1WindingHH;
    }

    public void setE1WindingHH(String e1WindingHH) {
        this.e1WindingHH = e1WindingHH;
    }

    public String getE1UHH() {
        return e1UHH;
    }

    public void setE1UHH(String e1UHH) {
        this.e1UHH = e1UHH;
    }

    public String getE1R15HH() {
        return e1R15HH;
    }

    public void setE1R15HH(String e1R15HH) {
        this.e1R15HH = e1R15HH;
    }

    public String getE1R60HH() {
        return e1R60HH;
    }

    public void setE1R60HH(String e1R60HH) {
        this.e1R60HH = e1R60HH;
    }

    public String getE1CoefHH() {
        return e1CoefHH;
    }

    public void setE1CoefHH(String e1CoefHH) {
        this.e1CoefHH = e1CoefHH;
    }

    public String getE1ResultHH() {
        return e1ResultHH;
    }

    public void setE1ResultHH(String e1ResultHH) {
        this.e1ResultHH = e1ResultHH;
    }

    public String getE2WindingBH() {
        return e2WindingBH;
    }

    public void setE2WindingBH(String e2WindingBH) {
        this.e2WindingBH = e2WindingBH;
    }

    public String getE2ABBH() {
        return e2ABBH;
    }

    public void setE2ABBH(String e2ABBH) {
        this.e2ABBH = e2ABBH;
    }

    public String getE2BCBH() {
        return e2BCBH;
    }

    public void setE2BCBH(String e2BCBH) {
        this.e2BCBH = e2BCBH;
    }

    public String getE2CABH() {
        return e2CABH;
    }

    public void setE2CABH(String e2CABH) {
        this.e2CABH = e2CABH;
    }

    public String getE2TBH() {
        return e2TBH;
    }

    public void setE2TBH(String e2TBH) {
        this.e2TBH = e2TBH;
    }

    public String getE2ResultBH() {
        return e2ResultBH;
    }

    public void setE2ResultBH(String e2ResultBH) {
        this.e2ResultBH = e2ResultBH;
    }

    public String getE2WindingHH() {
        return e2WindingHH;
    }

    public void setE2WindingHH(String e2WindingHH) {
        this.e2WindingHH = e2WindingHH;
    }

    public String getE2ABHH() {
        return e2ABHH;
    }

    public void setE2ABHH(String e2ABHH) {
        this.e2ABHH = e2ABHH;
    }

    public String getE2BCHH() {
        return e2BCHH;
    }

    public void setE2BCHH(String e2BCHH) {
        this.e2BCHH = e2BCHH;
    }

    public String getE2CAHH() {
        return e2CAHH;
    }

    public void setE2CAHH(String e2CAHH) {
        this.e2CAHH = e2CAHH;
    }

    public String getE2THH() {
        return e2THH;
    }

    public void setE2THH(String e2THH) {
        this.e2THH = e2THH;
    }

    public String getE2ResultHH() {
        return e2ResultHH;
    }

    public void setE2ResultHH(String e2ResultHH) {
        this.e2ResultHH = e2ResultHH;
    }

    public String getE3UInputAB() {
        return e3UInputAB;
    }

    public void setE3UInputAB(String e3UInputAB) {
        this.e3UInputAB = e3UInputAB;
    }

    public String getE3UInputBC() {
        return e3UInputBC;
    }

    public void setE3UInputBC(String e3UInputBC) {
        this.e3UInputBC = e3UInputBC;
    }

    public String getE3UInputCA() {
        return e3UInputCA;
    }

    public void setE3UInputCA(String e3UInputCA) {
        this.e3UInputCA = e3UInputCA;
    }

    public String getE3UInputAvr() {
        return e3UInputAvr;
    }

    public void setE3UInputAvr(String e3UInputAvr) {
        this.e3UInputAvr = e3UInputAvr;
    }

    public String getE3UOutputAB() {
        return e3UOutputAB;
    }

    public void setE3UOutputAB(String e3UOutputAB) {
        this.e3UOutputAB = e3UOutputAB;
    }

    public String getE3UOutputBC() {
        return e3UOutputBC;
    }

    public void setE3UOutputBC(String e3UOutputBC) {
        this.e3UOutputBC = e3UOutputBC;
    }

    public String getE3UOutputCA() {
        return e3UOutputCA;
    }

    public void setE3UOutputCA(String e3UOutputCA) {
        this.e3UOutputCA = e3UOutputCA;
    }

    public String getE3UOutputAvr() {
        return e3UOutputAvr;
    }

    public void setE3UOutputAvr(String e3UOutputAvr) {
        this.e3UOutputAvr = e3UOutputAvr;
    }

    public String getE3DiffU() {
        return e3DiffU;
    }

    public void setE3DiffU(String e3DiffU) {
        this.e3DiffU = e3DiffU;
    }

    public String getE3WindingBH() {
        return e3WindingBH;
    }

    public void setE3WindingBH(String e3WindingBH) {
        this.e3WindingBH = e3WindingBH;
    }

    public String getE3WindingHH() {
        return e3WindingHH;
    }

    public void setE3WindingHH(String e3WindingHH) {
        this.e3WindingHH = e3WindingHH;
    }

    public String getE3F() {
        return e3F;
    }

    public void setE3F(String e3F) {
        this.e3F = e3F;
    }

    public String getE3Result() {
        return e3Result;
    }

    public void setE3Result(String e3Result) {
        this.e3Result = e3Result;
    }

    public String getE4UKZVA() {
        return e4UKZVA;
    }

    public void setE4UKZVA(String e4UKZVA) {
        this.e4UKZVA = e4UKZVA;
    }

    public String getE4UKZVB() {
        return e4UKZVB;
    }

    public void setE4UKZVB(String e4UKZVB) {
        this.e4UKZVB = e4UKZVB;
    }

    public String getE4UKZVC() {
        return e4UKZVC;
    }

    public void setE4UKZVC(String e4UKZVC) {
        this.e4UKZVC = e4UKZVC;
    }

    public String getE4UKZPercent() {
        return e4UKZPercent;
    }

    public void setE4UKZPercent(String e4UKZPercent) {
        this.e4UKZPercent = e4UKZPercent;
    }

    public String getE4IA() {
        return e4IA;
    }

    public void setE4IA(String e4IA) {
        this.e4IA = e4IA;
    }

    public String getE4IB() {
        return e4IB;
    }

    public void setE4IB(String e4IB) {
        this.e4IB = e4IB;
    }

    public String getE4IC() {
        return e4IC;
    }

    public void setE4IC(String e4IC) {
        this.e4IC = e4IC;
    }

    public String getE4Pp() {
        return e4Pp;
    }

    public void setE4Pp(String e4Pp) {
        this.e4Pp = e4Pp;
    }

    public String getE4F() {
        return e4F;
    }

    public void setE4F(String e4F) {
        this.e4F = e4F;
    }

    public String getE4Result() {
        return e4Result;
    }

    public void setE4Result(String e4Result) {
        this.e4Result = e4Result;
    }

    public String getE5UBH() {
        return e5UBH;
    }

    public void setE5UBH(String e5UBH) {
        this.e5UBH = e5UBH;
    }

    public String getE5IA() {
        return e5IA;
    }

    public void setE5IA(String e5IA) {
        this.e5IA = e5IA;
    }

    public String getE5IB() {
        return e5IB;
    }

    public void setE5IB(String e5IB) {
        this.e5IB = e5IB;
    }

    public String getE5IC() {
        return e5IC;
    }

    public void setE5IC(String e5IC) {
        this.e5IC = e5IC;
    }

    public String getE5IAPercent() {
        return e5IAPercent;
    }

    public void setE5IAPercent(String e5IAPercent) {
        this.e5IAPercent = e5IAPercent;
    }

    public String getE5IBPercent() {
        return e5IBPercent;
    }

    public void setE5IBPercent(String e5IBPercent) {
        this.e5IBPercent = e5IBPercent;
    }

    public String getE5ICPercent() {
        return e5ICPercent;
    }

    public void setE5ICPercent(String e5ICPercent) {
        this.e5ICPercent = e5ICPercent;
    }

    public String getE5Pp() {
        return e5Pp;
    }

    public void setE5Pp(String e5Pp) {
        this.e5Pp = e5Pp;
    }

    public String getE5F() {
        return e5F;
    }

    public void setE5F(String e5F) {
        this.e5F = e5F;
    }

    public String getE5Result() {
        return e5Result;
    }

    public void setE5Result(String e5Result) {
        this.e5Result = e5Result;
    }

    public String getE6UInput() {
        return e6UInput;
    }

    public void setE6UInput(String e6UInput) {
        this.e6UInput = e6UInput;
    }

    public String getE6IBH() {
        return e6IBH;
    }

    public void setE6IBH(String e6IBH) {
        this.e6IBH = e6IBH;
    }

    public String getE6F() {
        return e6F;
    }

    public void setE6F(String e6F) {
        this.e6F = e6F;
    }

    public String getE6Time() {
        return e6Time;
    }

    public void setE6Time(String e6Time) {
        this.e6Time = e6Time;
    }

    public String getE6Result() {
        return e6Result;
    }

    public void setE6Result(String e6Result) {
        this.e6Result = e6Result;
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

    public double getUmeger() {
        return umeger;
    }

    public void setUmeger(double umeger) {
        this.umeger = umeger;
    }

    public String getPosition1() {
        return position1;
    }

    public void setPosition1(String position1) {
        this.position1 = position1;
    }

    public String getPosition1Number() {
        return position1Number;
    }

    public void setPosition1Number(String position1Number) {
        this.position1Number = position1Number;
    }

    public String getPosition1FullName() {
        return position1FullName;
    }

    public void setPosition1FullName(String position1FullName) {
        this.position1FullName = position1FullName;
    }

    public String getPosition2() {
        return position2;
    }

    public void setPosition2(String position2) {
        this.position2 = position2;
    }

    public String getPosition2Number() {
        return position2Number;
    }

    public void setPosition2Number(String position2Number) {
        this.position2Number = position2Number;
    }

    public String getPosition2FullName() {
        return position2FullName;
    }

    public void setPosition2FullName(String position2FullName) {
        this.position2FullName = position2FullName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("Время проведения испытания: HH:mm:ss");
        return String.format("%s. № %s (%s) %s", id, serialNumber, type, sdf.format(millis));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Protocol protocol = (Protocol) o;
        return id == protocol.id &&
                Double.compare(protocol.ubh, ubh) == 0 &&
                Double.compare(protocol.uhh, uhh) == 0 &&
                Double.compare(protocol.p, p) == 0 &&
                Double.compare(protocol.ixx, ixx) == 0 &&
                Double.compare(protocol.ukz, ukz) == 0 &&
                Double.compare(protocol.xxtime, xxtime) == 0 &&
                Double.compare(protocol.umeger, umeger) == 0 &&
                millis == protocol.millis &&
                Objects.equals(e1WindingBH, protocol.e1WindingBH) &&
                Objects.equals(e1UBH, protocol.e1UBH) &&
                Objects.equals(e1R15BH, protocol.e1R15BH) &&
                Objects.equals(e1R60BH, protocol.e1R60BH) &&
                Objects.equals(e1CoefBH, protocol.e1CoefBH) &&
                Objects.equals(e1ResultBH, protocol.e1ResultBH) &&
                Objects.equals(e1WindingHH, protocol.e1WindingHH) &&
                Objects.equals(e1UHH, protocol.e1UHH) &&
                Objects.equals(e1R15HH, protocol.e1R15HH) &&
                Objects.equals(e1R60HH, protocol.e1R60HH) &&
                Objects.equals(e1CoefHH, protocol.e1CoefHH) &&
                Objects.equals(e1ResultHH, protocol.e1ResultHH) &&
                Objects.equals(e2WindingBH, protocol.e2WindingBH) &&
                Objects.equals(e2ABBH, protocol.e2ABBH) &&
                Objects.equals(e2BCBH, protocol.e2BCBH) &&
                Objects.equals(e2CABH, protocol.e2CABH) &&
                Objects.equals(e2TBH, protocol.e2TBH) &&
                Objects.equals(e2ResultBH, protocol.e2ResultBH) &&
                Objects.equals(e2WindingHH, protocol.e2WindingHH) &&
                Objects.equals(e2ABHH, protocol.e2ABHH) &&
                Objects.equals(e2BCHH, protocol.e2BCHH) &&
                Objects.equals(e2CAHH, protocol.e2CAHH) &&
                Objects.equals(e2THH, protocol.e2THH) &&
                Objects.equals(e2ResultHH, protocol.e2ResultHH) &&
                Objects.equals(e3UInputAB, protocol.e3UInputAB) &&
                Objects.equals(e3UInputBC, protocol.e3UInputBC) &&
                Objects.equals(e3UInputCA, protocol.e3UInputCA) &&
                Objects.equals(e3UInputAvr, protocol.e3UInputAvr) &&
                Objects.equals(e3UOutputAB, protocol.e3UOutputAB) &&
                Objects.equals(e3UOutputBC, protocol.e3UOutputBC) &&
                Objects.equals(e3UOutputCA, protocol.e3UOutputCA) &&
                Objects.equals(e3UOutputAvr, protocol.e3UOutputAvr) &&
                Objects.equals(e3DiffU, protocol.e3DiffU) &&
                Objects.equals(e3WindingBH, protocol.e3WindingBH) &&
                Objects.equals(e3WindingHH, protocol.e3WindingHH) &&
                Objects.equals(e3F, protocol.e3F) &&
                Objects.equals(e3Result, protocol.e3Result) &&
                Objects.equals(e4UKZVA, protocol.e4UKZVA) &&
                Objects.equals(e4UKZVB, protocol.e4UKZVB) &&
                Objects.equals(e4UKZVC, protocol.e4UKZVC) &&
                Objects.equals(e4UKZPercent, protocol.e4UKZPercent) &&
                Objects.equals(e4IA, protocol.e4IA) &&
                Objects.equals(e4IB, protocol.e4IB) &&
                Objects.equals(e4IC, protocol.e4IC) &&
                Objects.equals(e4Pp, protocol.e4Pp) &&
                Objects.equals(e4F, protocol.e4F) &&
                Objects.equals(e4Result, protocol.e4Result) &&
                Objects.equals(e5UBH, protocol.e5UBH) &&
                Objects.equals(e5IA, protocol.e5IA) &&
                Objects.equals(e5IB, protocol.e5IB) &&
                Objects.equals(e5IC, protocol.e5IC) &&
                Objects.equals(e5IAPercent, protocol.e5IAPercent) &&
                Objects.equals(e5IBPercent, protocol.e5IBPercent) &&
                Objects.equals(e5ICPercent, protocol.e5ICPercent) &&
                Objects.equals(e5Pp, protocol.e5Pp) &&
                Objects.equals(e5F, protocol.e5F) &&
                Objects.equals(e5Result, protocol.e5Result) &&
                Objects.equals(e6UInput, protocol.e6UInput) &&
                Objects.equals(e6IBH, protocol.e6IBH) &&
                Objects.equals(e6F, protocol.e6F) &&
                Objects.equals(e6Time, protocol.e6Time) &&
                Objects.equals(e6Result, protocol.e6Result) &&
                Objects.equals(serialNumber, protocol.serialNumber) &&
                Objects.equals(type, protocol.type) &&
                Objects.equals(position1, protocol.position1) &&
                Objects.equals(position1Number, protocol.position1Number) &&
                Objects.equals(position1FullName, protocol.position1FullName) &&
                Objects.equals(position2, protocol.position2) &&
                Objects.equals(position2Number, protocol.position2Number) &&
                Objects.equals(position2FullName, protocol.position2FullName) &&
                Objects.equals(date, protocol.date) &&
                Objects.equals(time, protocol.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, e1WindingBH, e1UBH, e1R15BH, e1R60BH, e1CoefBH, e1ResultBH, e1WindingHH, e1UHH, e1R15HH, e1R60HH, e1CoefHH, e1ResultHH, e2WindingBH, e2ABBH, e2BCBH, e2CABH, e2TBH, e2ResultBH, e2WindingHH, e2ABHH, e2BCHH, e2CAHH, e2THH, e2ResultHH, e3UInputAB, e3UInputBC, e3UInputCA, e3UInputAvr, e3UOutputAB, e3UOutputBC, e3UOutputCA, e3UOutputAvr, e3DiffU, e3WindingBH, e3WindingHH, e3F, e3Result, e4UKZVA, e4UKZVB, e4UKZVC, e4UKZPercent, e4IA, e4IB, e4IC, e4Pp, e4F, e4Result, e5UBH, e5IA, e5IB, e5IC, e5IAPercent, e5IBPercent, e5ICPercent, e5Pp, e5F, e5Result, e6UInput, e6IBH, e6F, e6Time, e6Result, serialNumber, type, ubh, uhh, p, ixx, ukz, xxtime, umeger, position1, position1Number, position1FullName, position2, position2Number, position2FullName, millis, date, time);
    }
}
