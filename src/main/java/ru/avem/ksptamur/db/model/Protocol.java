package ru.avem.ksptamur.db.model;

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
    private String e0WindingBH = "";
    @DatabaseField
    private String e0UBH = "";
    @DatabaseField
    private String e0R15BH = "";
    @DatabaseField
    private String e0R60BH = "";
    @DatabaseField
    private String e0CoefBH = "";
    @DatabaseField
    private String e0TBH = "";
    @DatabaseField
    private String e0ResultBH = "";

    @DatabaseField
    private String e0WindingHH = "";
    @DatabaseField
    private String e0UHH = "";
    @DatabaseField
    private String e0R15HH = "";
    @DatabaseField
    private String e0R60HH = "";
    @DatabaseField
    private String e0CoefHH = "";
    @DatabaseField
    private String e0THH = "";
    @DatabaseField
    private String e0ResultHH = "";

    @DatabaseField
    private String e0WindingBHHH = "";
    @DatabaseField
    private String e0UBHHH = "";
    @DatabaseField
    private String e0R15BHHH = "";
    @DatabaseField
    private String e0R60BHHH = "";
    @DatabaseField
    private String e0CoefBHHH = "";
    @DatabaseField
    private String e0TBHHH = "";
    @DatabaseField
    private String e0ResultBHHH = "";

    @DatabaseField
    private String e1WindingBH = "";
    @DatabaseField
    private String e1ABBH = "";
    @DatabaseField
    private String e1BCBH = "";
    @DatabaseField
    private String e1CABH = "";
    @DatabaseField
    private String e1TBH = "";
    @DatabaseField
    private String e1ResultBH = "";

    @DatabaseField
    private String e1WindingHH = "";
    @DatabaseField
    private String e1ABHH = "";
    @DatabaseField
    private String e1BCHH = "";
    @DatabaseField
    private String e1CAHH = "";
    @DatabaseField
    private String e1THH = "";
    @DatabaseField
    private String e1ResultHH = "";

    @DatabaseField
    private String e2UInputAB = "";
    @DatabaseField
    private String e2UInputBC = "";
    @DatabaseField
    private String e2UInputCA = "";
    @DatabaseField
    private String e2UInputAvr = "";
    @DatabaseField
    private String e2UOutputAB = "";
    @DatabaseField
    private String e2UOutputBC = "";
    @DatabaseField
    private String e2UOutputCA = "";
    @DatabaseField
    private String e2UOutputAvr = "";
    @DatabaseField
    private String e2DiffU = "";
    @DatabaseField
    private String e2F = "";
    @DatabaseField
    private String e2Result = "";

    @DatabaseField
    private String e3UBH = "";
    @DatabaseField
    private String e3UHH = "";
    @DatabaseField
    private String e3F = "";
    @DatabaseField
    private String e3Result = "";

    @DatabaseField
    private String e4WindingBH = "";
    @DatabaseField
    private String e4WindingHH = "";
    @DatabaseField
    private String e4UBH = "";
    @DatabaseField
    private String e4UHH = "";
    @DatabaseField
    private String e4Result = "";

    @DatabaseField
    private String e5UKZV = "";
    @DatabaseField
    private String e5UKZPercent = "";
    @DatabaseField
    private String e5UKZDiff = "";
    @DatabaseField
    private String e5IA = "";
    @DatabaseField
    private String e5IB = "";
    @DatabaseField
    private String e5IC = "";
    @DatabaseField
    private String e5Pp = "";
    @DatabaseField
    private String e5F = "";
    @DatabaseField
    private String e5Result = "";

    @DatabaseField
    private String e6UBH = "";
    @DatabaseField
    private String e6IA = "";
    @DatabaseField
    private String e6IB = "";
    @DatabaseField
    private String e6IC = "";
    @DatabaseField
    private String e6IAPercent = "";
    @DatabaseField
    private String e6IBPercent = "";
    @DatabaseField
    private String e6ICPercent = "";
    @DatabaseField
    private String e6IADiff = "";
    @DatabaseField
    private String e6IBDiff = "";
    @DatabaseField
    private String e6ICDiff = "";
    @DatabaseField
    private String e6Pp = "";
    @DatabaseField
    private String e6Cos = "";
    @DatabaseField
    private String e6F = "";
    @DatabaseField
    private String e6Result = "";

    @DatabaseField
    private String e7UInput = "";
    @DatabaseField
    private String e7IBH = "";
    @DatabaseField
    private String e7F = "";
    @DatabaseField
    private String e7Time = "";
    @DatabaseField
    private String e7Result = "";


    @DatabaseField
    private String e8TypeBHandCorps = "";
    @DatabaseField
    private String e8IBHandCorps = "";
    @DatabaseField
    private String e8UBHandCorps = "";
    @DatabaseField
    private String e8TimeBHandCorps = "";
    @DatabaseField
    private String e8ResultBHandCorps = "";
    @DatabaseField
    private String e8TypeHHandCorps = "";
    @DatabaseField
    private String e8IHHandCorps = "";
    @DatabaseField
    private String e8UHHandCorps = "";
    @DatabaseField
    private String e8TimeHHandCorps = "";
    @DatabaseField
    private String e8ResultHHandCorps = "";




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
    private double phase;
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

    public Protocol(String serialNumber, ru.avem.ksptamur.db.model.TestItem selectedTestItem, ru.avem.ksptamur.db.model.Account firstTester, ru.avem.ksptamur.db.model.Account secondTester, long millis) {
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

    public ru.avem.ksptamur.db.model.TestItem getObject() {
        return new ru.avem.ksptamur.db.model.TestItem(type, ubh,
                uhh, p, phase, ixx, ukz, xxtime, uinsulation, umeger);
    }

    public void setObject(ru.avem.ksptamur.db.model.TestItem object) {
        type = object.getType();
        ubh = object.getUbh();
        uhh = object.getUhh();
        p = object.getP();
        phase = object.getPhase();
        ixx = object.getIxx();
        ukz = object.getUkz();
        xxtime = object.getXxtime();
        uinsulation = object.getUinsulation();
        umeger = object.getUmeger();
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
        this.date = new SimpleDateFormat("dd.MM.yy").format(millis);
    }

    public String getE0UBH() {
        return e0UBH;
    }

    public void setE0UBH(String e0UBH) {
        this.e0UBH = e0UBH;
    }

    public String getE0UHH() {
        return e0UHH;
    }

    public void setE0UHH(String e0UHH) {
        this.e0UHH = e0UHH;
    }

    public String getE0UBHHH() {
        return e0UBHHH;
    }

    public void setE0UBHHH(String e0UBHHH) {
        this.e0UBHHH = e0UBHHH;
    }

    public String getE0WindingBH() {
        return e0WindingBH;
    }

    public void setE0WindingBH(String e0WindingBH) {
        this.e0WindingBH = e0WindingBH;
    }

    public String getE0R15BH() {
        return e0R15BH;
    }

    public void setE0R15BH(String e0R15BH) {
        this.e0R15BH = e0R15BH;
    }

    public String getE0R60BH() {
        return e0R60BH;
    }

    public void setE0R60BH(String e0R60BH) {
        this.e0R60BH = e0R60BH;
    }

    public String getE0CoefBH() {
        return e0CoefBH;
    }

    public void setE0CoefBH(String e0CoefBH) {
        this.e0CoefBH = e0CoefBH;
    }

    public String getE0TBH() {
        return e0TBH;
    }

    public void setE0TBH(String e0TBH) {
        this.e0TBH = e0TBH;
    }

    public String getE0ResultBH() {
        return e0ResultBH;
    }

    public void setE0ResultBH(String e0ResultBH) {
        this.e0ResultBH = e0ResultBH;
    }

    public String getE0WindingHH() {
        return e0WindingHH;
    }

    public void setE0WindingHH(String e0WindingHH) {
        this.e0WindingHH = e0WindingHH;
    }

    public String getE0R15HH() {
        return e0R15HH;
    }

    public void setE0R15HH(String e0R15HH) {
        this.e0R15HH = e0R15HH;
    }

    public String getE0R60HH() {
        return e0R60HH;
    }

    public void setE0R60HH(String e0R60HH) {
        this.e0R60HH = e0R60HH;
    }

    public String getE0CoefHH() {
        return e0CoefHH;
    }

    public void setE0CoefHH(String e0CoefHH) {
        this.e0CoefHH = e0CoefHH;
    }

    public String getE0THH() {
        return e0THH;
    }

    public void setE0THH(String e0THH) {
        this.e0THH = e0THH;
    }

    public String getE0ResultHH() {
        return e0ResultHH;
    }

    public void setE0ResultHH(String e0ResultHH) {
        this.e0ResultHH = e0ResultHH;
    }

    public String getE0WindingBHHH() {
        return e0WindingBHHH;
    }

    public void setE0WindingBHHH(String e0WindingBHHH) {
        this.e0WindingBHHH = e0WindingBHHH;
    }

    public String getE0R15BHHH() {
        return e0R15BHHH;
    }

    public void setE0R15BHHH(String e0R15BHHH) {
        this.e0R15BHHH = e0R15BHHH;
    }

    public String getE0R60BHHH() {
        return e0R60BHHH;
    }

    public void setE0R60BHHH(String e0R60BHHH) {
        this.e0R60BHHH = e0R60BHHH;
    }

    public String getE0CoefBHHH() {
        return e0CoefBHHH;
    }

    public void setE0CoefBHHH(String e0CoefBHHH) {
        this.e0CoefBHHH = e0CoefBHHH;
    }

    public String getE0TBHHH() {
        return e0TBHHH;
    }

    public void setE0TBHHH(String e0TBHHH) {
        this.e0TBHHH = e0TBHHH;
    }

    public String getE0ResultBHHH() {
        return e0ResultBHHH;
    }

    public void setE0ResultBHHH(String e0ResultBHHH) {
        this.e0ResultBHHH = e0ResultBHHH;
    }

    public String getE1WindingBH() {
        return e1WindingBH;
    }

    public void setE1WindingBH(String e1WindingBH) {
        this.e1WindingBH = e1WindingBH;
    }

    public String getE1ABBH() {
        return e1ABBH;
    }

    public void setE1ABBH(String e1ABBH) {
        this.e1ABBH = e1ABBH;
    }

    public String getE1BCBH() {
        return e1BCBH;
    }

    public void setE1BCBH(String e1BCBH) {
        this.e1BCBH = e1BCBH;
    }

    public String getE1CABH() {
        return e1CABH;
    }

    public void setE1CABH(String e1CABH) {
        this.e1CABH = e1CABH;
    }

    public String getE1TBH() {
        return e1TBH;
    }

    public void setE1TBH(String e1TBH) {
        this.e1TBH = e1TBH;
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

    public String getE1ABHH() {
        return e1ABHH;
    }

    public void setE1ABHH(String e1ABHH) {
        this.e1ABHH = e1ABHH;
    }

    public String getE1BCHH() {
        return e1BCHH;
    }

    public void setE1BCHH(String e1BCHH) {
        this.e1BCHH = e1BCHH;
    }

    public String getE1CAHH() {
        return e1CAHH;
    }

    public void setE1CAHH(String e1CAHH) {
        this.e1CAHH = e1CAHH;
    }

    public String getE1THH() {
        return e1THH;
    }

    public void setE1THH(String e1THH) {
        this.e1THH = e1THH;
    }

    public String getE1ResultHH() {
        return e1ResultHH;
    }

    public void setE1ResultHH(String e1ResultHH) {
        this.e1ResultHH = e1ResultHH;
    }

    public String getE2UInputAB() {
        return e2UInputAB;
    }

    public void setE2UInputAB(String e2UInputAB) {
        this.e2UInputAB = e2UInputAB;
    }

    public String getE2UInputBC() {
        return e2UInputBC;
    }

    public void setE2UInputBC(String e2UInputBC) {
        this.e2UInputBC = e2UInputBC;
    }

    public String getE2UInputCA() {
        return e2UInputCA;
    }

    public void setE2UInputCA(String e2UInputCA) {
        this.e2UInputCA = e2UInputCA;
    }

    public String getE2UInputAvr() {
        return e2UInputAvr;
    }

    public void setE2UInputAvr(String e2UInputAvr) {
        this.e2UInputAvr = e2UInputAvr;
    }

    public String getE2UOutputAB() {
        return e2UOutputAB;
    }

    public void setE2UOutputAB(String e2UOutputAB) {
        this.e2UOutputAB = e2UOutputAB;
    }

    public String getE2UOutputBC() {
        return e2UOutputBC;
    }

    public void setE2UOutputBC(String e2UOutputBC) {
        this.e2UOutputBC = e2UOutputBC;
    }

    public String getE2UOutputCA() {
        return e2UOutputCA;
    }

    public void setE2UOutputCA(String e2UOutputCA) {
        this.e2UOutputCA = e2UOutputCA;
    }

    public String getE2UOutputAvr() {
        return e2UOutputAvr;
    }

    public void setE2UOutputAvr(String e2UOutputAvr) {
        this.e2UOutputAvr = e2UOutputAvr;
    }

    public String getE2DiffU() {
        return e2DiffU;
    }

    public void setE2DiffU(String e2DiffU) {
        this.e2DiffU = e2DiffU;
    }

    public String getE2F() {
        return e2F;
    }

    public void setE2F(String e2F) {
        this.e2F = e2F;
    }

    public String getE2Result() {
        return e2Result;
    }

    public void setE2Result(String e2Result) {
        this.e2Result = e2Result;
    }

    public String getE3UBH() {
        return e3UBH;
    }

    public void setE3UBH(String e3UBH) {
        this.e3UBH = e3UBH;
    }

    public String getE3UHH() {
        return e3UHH;
    }

    public void setE3UHH(String e3UHH) {
        this.e3UHH = e3UHH;
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

    public String getE4WindingBH() {
        return e4WindingBH;
    }

    public void setE4WindingBH(String e4WindingBH) {
        this.e4WindingBH = e4WindingBH;
    }

    public String getE4WindingHH() {
        return e4WindingHH;
    }

    public void setE4WindingHH(String e4WindingHH) {
        this.e4WindingHH = e4WindingHH;
    }

    public String getE4UBH() {
        return e4UBH;
    }

    public void setE4UBH(String e4UBH) {
        this.e4UBH = e4UBH;
    }

    public String getE4UHH() {
        return e4UHH;
    }

    public void setE4UHH(String e4UHH) {
        this.e4UHH = e4UHH;
    }

    public String getE4Result() {
        return e4Result;
    }

    public void setE4Result(String e4Result) {
        this.e4Result = e4Result;
    }

    public String getE5UKZV() {
        return e5UKZV;
    }

    public void setE5UKZV(String e5UKZV) {
        this.e5UKZV = e5UKZV;
    }

    public String getE5UKZPercent() {
        return e5UKZPercent;
    }

    public void setE5UKZPercent(String e5UKZPercent) {
        this.e5UKZPercent = e5UKZPercent;
    }

    public String getE5UKZDiff() {
        return e5UKZDiff;
    }

    public void setE5UKZDiff(String e5UKZDiff) {
        this.e5UKZDiff = e5UKZDiff;
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

    public String getE6UBH() {
        return e6UBH;
    }

    public void setE6UBH(String e6UBH) {
        this.e6UBH = e6UBH;
    }

    public String getE6IA() {
        return e6IA;
    }

    public void setE6IA(String e6IA) {
        this.e6IA = e6IA;
    }

    public String getE6IB() {
        return e6IB;
    }

    public void setE6IB(String e6IB) {
        this.e6IB = e6IB;
    }

    public String getE6IC() {
        return e6IC;
    }

    public void setE6IC(String e6IC) {
        this.e6IC = e6IC;
    }

    public String getE6IAPercent() {
        return e6IAPercent;
    }

    public void setE6IAPercent(String e6IAPercent) {
        this.e6IAPercent = e6IAPercent;
    }

    public String getE6IBPercent() {
        return e6IBPercent;
    }

    public void setE6IBPercent(String e6IBPercent) {
        this.e6IBPercent = e6IBPercent;
    }

    public String getE6ICPercent() {
        return e6ICPercent;
    }

    public void setE6ICPercent(String e6ICPercent) {
        this.e6ICPercent = e6ICPercent;
    }

    public String getE6IADiff() {
        return e6IADiff;
    }

    public void setE6IADiff(String e6IADiff) {
        this.e6IADiff = e6IADiff;
    }

    public String getE6IBDiff() {
        return e6IBDiff;
    }

    public void setE6IBDiff(String e6IBDiff) {
        this.e6IBDiff = e6IBDiff;
    }

    public String getE6ICDiff() {
        return e6ICDiff;
    }

    public void setE6ICDiff(String e6ICDiff) {
        this.e6ICDiff = e6ICDiff;
    }

    public String getE6Pp() {
        return e6Pp;
    }

    public void setE6Pp(String e6Pp) {
        this.e6Pp = e6Pp;
    }

    public String getE6Cos() {
        return e6Cos;
    }

    public void setE6Cos(String e6Cos) {
        this.e6Cos = e6Cos;
    }

    public String getE6F() {
        return e6F;
    }

    public void setE6F(String e6F) {
        this.e6F = e6F;
    }

    public String getE6Result() {
        return e6Result;
    }

    public void setE6Result(String e6Result) {
        this.e6Result = e6Result;
    }

    public String getE7UInput() {
        return e7UInput;
    }

    public void setE7UInput(String e7UInput) {
        this.e7UInput = e7UInput;
    }

    public String getE7IBH() {
        return e7IBH;
    }

    public void setE7IBH(String e7IBH) {
        this.e7IBH = e7IBH;
    }

    public String getE7F() {
        return e7F;
    }

    public void setE7F(String e7F) {
        this.e7F = e7F;
    }

    public String getE7Time() {
        return e7Time;
    }

    public void setE7Time(String e7Time) {
        this.e7Time = e7Time;
    }

    public String getE7Result() {
        return e7Result;
    }

    public void setE7Result(String e7Result) {
        this.e7Result = e7Result;
    }

    public String getE8TypeBHandCorps() {
        return e8TypeBHandCorps;
    }

    public void setE8TypeBHandCorps(String e8TypeBHandCorps) {
        this.e8TypeBHandCorps = e8TypeBHandCorps;
    }

    public String getE8IBHandCorps() {
        return e8IBHandCorps;
    }

    public void setE8IBHandCorps(String e8IBHandCorps) {
        this.e8IBHandCorps = e8IBHandCorps;
    }

    public String getE8UBHandCorps() {
        return e8UBHandCorps;
    }

    public void setE8UBHandCorps(String e8UBHandCorps) {
        this.e8UBHandCorps = e8UBHandCorps;
    }

    public String getE8TimeBHandCorps() {
        return e8TimeBHandCorps;
    }

    public void setE8TimeBHandCorps(String e8TimeBHandCorps) {
        this.e8TimeBHandCorps = e8TimeBHandCorps;
    }

    public String getE8ResultBHandCorps() {
        return e8ResultBHandCorps;
    }

    public void setE8ResultBHandCorps(String e8ResultBHandCorps) {
        this.e8ResultBHandCorps = e8ResultBHandCorps;
    }

    public String getE8TypeHHandCorps() {
        return e8TypeHHandCorps;
    }

    public void setE8TypeHHandCorps(String e8TypeHHandCorps) {
        this.e8TypeHHandCorps = e8TypeHHandCorps;
    }

    public String getE8IHHandCorps() {
        return e8IHHandCorps;
    }

    public void setE8IHHandCorps(String e8IHHandCorps) {
        this.e8IHHandCorps = e8IHHandCorps;
    }

    public String getE8UHHandCorps() {
        return e8UHHandCorps;
    }

    public void setE8UHHandCorps(String e8UHHandCorps) {
        this.e8UHHandCorps = e8UHHandCorps;
    }

    public String getE8TimeHHandCorps() {
        return e8TimeHHandCorps;
    }

    public void setE8TimeHHandCorps(String e8TimeHHandCorps) {
        this.e8TimeHHandCorps = e8TimeHHandCorps;
    }

    public String getE8ResultHHandCorps() {
        return e8ResultHHandCorps;
    }

    public void setE8ResultHHandCorps(String e8ResultHHandCorps) {
        this.e8ResultHHandCorps = e8ResultHHandCorps;
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

    public double getPhase() {
        return phase;
    }

    public void setPhase(double phase) {
        this.phase = phase;
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
                Double.compare(protocol.phase, phase) == 0 &&
                Double.compare(protocol.ixx, ixx) == 0 &&
                Double.compare(protocol.ukz, ukz) == 0 &&
                Double.compare(protocol.xxtime, xxtime) == 0 &&
                Double.compare(protocol.uinsulation, uinsulation) == 0 &&
                Double.compare(protocol.umeger, umeger) == 0 &&
                millis == protocol.millis &&
                Objects.equals(e0WindingBH, protocol.e0WindingBH) &&
                Objects.equals(e0UBH, protocol.e0UBH) &&
                Objects.equals(e0R15BH, protocol.e0R15BH) &&
                Objects.equals(e0R60BH, protocol.e0R60BH) &&
                Objects.equals(e0CoefBH, protocol.e0CoefBH) &&
                Objects.equals(e0TBH, protocol.e0TBH) &&
                Objects.equals(e0ResultBH, protocol.e0ResultBH) &&
                Objects.equals(e0WindingHH, protocol.e0WindingHH) &&
                Objects.equals(e0UHH, protocol.e0UHH) &&
                Objects.equals(e0R15HH, protocol.e0R15HH) &&
                Objects.equals(e0R60HH, protocol.e0R60HH) &&
                Objects.equals(e0CoefHH, protocol.e0CoefHH) &&
                Objects.equals(e0THH, protocol.e0THH) &&
                Objects.equals(e0ResultHH, protocol.e0ResultHH) &&
                Objects.equals(e0WindingBHHH, protocol.e0WindingBHHH) &&
                Objects.equals(e0UBHHH, protocol.e0UBHHH) &&
                Objects.equals(e0R15BHHH, protocol.e0R15BHHH) &&
                Objects.equals(e0R60BHHH, protocol.e0R60BHHH) &&
                Objects.equals(e0CoefBHHH, protocol.e0CoefBHHH) &&
                Objects.equals(e0TBHHH, protocol.e0TBHHH) &&
                Objects.equals(e0ResultBHHH, protocol.e0ResultBHHH) &&
                Objects.equals(e1WindingBH, protocol.e1WindingBH) &&
                Objects.equals(e1ABBH, protocol.e1ABBH) &&
                Objects.equals(e1BCBH, protocol.e1BCBH) &&
                Objects.equals(e1CABH, protocol.e1CABH) &&
                Objects.equals(e1TBH, protocol.e1TBH) &&
                Objects.equals(e1ResultBH, protocol.e1ResultBH) &&
                Objects.equals(e1WindingHH, protocol.e1WindingHH) &&
                Objects.equals(e1ABHH, protocol.e1ABHH) &&
                Objects.equals(e1BCHH, protocol.e1BCHH) &&
                Objects.equals(e1CAHH, protocol.e1CAHH) &&
                Objects.equals(e1THH, protocol.e1THH) &&
                Objects.equals(e1ResultHH, protocol.e1ResultHH) &&
                Objects.equals(e2UInputAB, protocol.e2UInputAB) &&
                Objects.equals(e2UInputBC, protocol.e2UInputBC) &&
                Objects.equals(e2UInputCA, protocol.e2UInputCA) &&
                Objects.equals(e2UInputAvr, protocol.e2UInputAvr) &&
                Objects.equals(e2UOutputAB, protocol.e2UOutputAB) &&
                Objects.equals(e2UOutputBC, protocol.e2UOutputBC) &&
                Objects.equals(e2UOutputCA, protocol.e2UOutputCA) &&
                Objects.equals(e2UOutputAvr, protocol.e2UOutputAvr) &&
                Objects.equals(e2DiffU, protocol.e2DiffU) &&
                Objects.equals(e2F, protocol.e2F) &&
                Objects.equals(e2Result, protocol.e2Result) &&
                Objects.equals(e3UBH, protocol.e3UBH) &&
                Objects.equals(e3UHH, protocol.e3UHH) &&
                Objects.equals(e3F, protocol.e3F) &&
                Objects.equals(e3Result, protocol.e3Result) &&
                Objects.equals(e4WindingBH, protocol.e4WindingBH) &&
                Objects.equals(e4WindingHH, protocol.e4WindingHH) &&
                Objects.equals(e4UBH, protocol.e4UBH) &&
                Objects.equals(e4UHH, protocol.e4UHH) &&
                Objects.equals(e4Result, protocol.e4Result) &&
                Objects.equals(e5UKZV, protocol.e5UKZV) &&
                Objects.equals(e5UKZPercent, protocol.e5UKZPercent) &&
                Objects.equals(e5UKZDiff, protocol.e5UKZDiff) &&
                Objects.equals(e5IA, protocol.e5IA) &&
                Objects.equals(e5IB, protocol.e5IB) &&
                Objects.equals(e5IC, protocol.e5IC) &&
                Objects.equals(e5Pp, protocol.e5Pp) &&
                Objects.equals(e5F, protocol.e5F) &&
                Objects.equals(e5Result, protocol.e5Result) &&
                Objects.equals(e6UBH, protocol.e6UBH) &&
                Objects.equals(e6IA, protocol.e6IA) &&
                Objects.equals(e6IB, protocol.e6IB) &&
                Objects.equals(e6IC, protocol.e6IC) &&
                Objects.equals(e6IAPercent, protocol.e6IAPercent) &&
                Objects.equals(e6IBPercent, protocol.e6IBPercent) &&
                Objects.equals(e6ICPercent, protocol.e6ICPercent) &&
                Objects.equals(e6IADiff, protocol.e6IADiff) &&
                Objects.equals(e6IBDiff, protocol.e6IBDiff) &&
                Objects.equals(e6ICDiff, protocol.e6ICDiff) &&
                Objects.equals(e6Pp, protocol.e6Pp) &&
                Objects.equals(e6Cos, protocol.e6Cos) &&
                Objects.equals(e6F, protocol.e6F) &&
                Objects.equals(e6Result, protocol.e6Result) &&
                Objects.equals(e7UInput, protocol.e7UInput) &&
                Objects.equals(e7IBH, protocol.e7IBH) &&
                Objects.equals(e7F, protocol.e7F) &&
                Objects.equals(e7Time, protocol.e7Time) &&
                Objects.equals(e7Result, protocol.e7Result) &&
                Objects.equals(e8TypeBHandCorps, protocol.e8TypeBHandCorps) &&
                Objects.equals(e8IBHandCorps, protocol.e8IBHandCorps) &&
                Objects.equals(e8UBHandCorps, protocol.e8UBHandCorps) &&
                Objects.equals(e8TimeBHandCorps, protocol.e8TimeBHandCorps) &&
                Objects.equals(e8ResultBHandCorps, protocol.e8ResultBHandCorps) &&
                Objects.equals(e8TypeHHandCorps, protocol.e8TypeHHandCorps) &&
                Objects.equals(e8IHHandCorps, protocol.e8IHHandCorps) &&
                Objects.equals(e8UHHandCorps, protocol.e8UHHandCorps) &&
                Objects.equals(e8TimeHHandCorps, protocol.e8TimeHHandCorps) &&
                Objects.equals(e8ResultHHandCorps, protocol.e8ResultHHandCorps) &&
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
        return Objects.hash(id, e0WindingBH, e0UBH, e0R15BH, e0R60BH, e0CoefBH, e0TBH, e0ResultBH, e0WindingHH, e0UHH, e0R15HH, e0R60HH, e0CoefHH, e0THH, e0ResultHH, e0WindingBHHH, e0UBHHH, e0R15BHHH, e0R60BHHH, e0CoefBHHH, e0TBHHH, e0ResultBHHH, e1WindingBH, e1ABBH, e1BCBH, e1CABH, e1TBH, e1ResultBH, e1WindingHH, e1ABHH, e1BCHH, e1CAHH, e1THH, e1ResultHH, e2UInputAB, e2UInputBC, e2UInputCA, e2UInputAvr, e2UOutputAB, e2UOutputBC, e2UOutputCA, e2UOutputAvr, e2DiffU, e2F, e2Result, e3UBH, e3UHH, e3F, e3Result, e4WindingBH, e4WindingHH, e4UBH, e4UHH, e4Result, e5UKZV, e5UKZPercent, e5UKZDiff, e5IA, e5IB, e5IC, e5Pp, e5F, e5Result, e6UBH, e6IA, e6IB, e6IC, e6IAPercent, e6IBPercent, e6ICPercent, e6IADiff, e6IBDiff, e6ICDiff, e6Pp, e6Cos, e6F, e6Result, e7UInput, e7IBH, e7F, e7Time, e7Result, e8TypeBHandCorps, e8IBHandCorps, e8UBHandCorps, e8TimeBHandCorps, e8ResultBHandCorps, e8TypeHHandCorps, e8IHHandCorps, e8UHHandCorps, e8TimeHHandCorps, e8ResultHHandCorps, serialNumber, type, ubh, uhh, p, phase, ixx, ukz, xxtime, uinsulation, umeger, position1, position1Number, position1FullName, position2, position2Number, position2FullName, millis, date, time);
    }
}
