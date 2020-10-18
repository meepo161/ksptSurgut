package ru.avem.ksptsurgut.communication;

import ru.avem.ksptsurgut.Constants;
import ru.avem.ksptsurgut.communication.connections.Connection;
import ru.avem.ksptsurgut.communication.connections.SerialConnection;
import ru.avem.ksptsurgut.communication.devices.DeviceController;
import ru.avem.ksptsurgut.communication.devices.avem_voltmeter.AvemVoltmeterController;
import ru.avem.ksptsurgut.communication.devices.cs02021.CS02021Controller;
import ru.avem.ksptsurgut.communication.devices.deltaC2000.DeltaCP2000Controller;
import ru.avem.ksptsurgut.communication.devices.ikas.IKASController;
import ru.avem.ksptsurgut.communication.devices.parmaT400.ParmaT400Controller;
import ru.avem.ksptsurgut.communication.devices.phasemeter.PhaseMeterController;
import ru.avem.ksptsurgut.communication.devices.pm130.PM130Controller;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRController;
import ru.avem.ksptsurgut.communication.devices.trm.TRMController;
import ru.avem.ksptsurgut.communication.modbus.ModbusController;
import ru.avem.ksptsurgut.communication.modbus.RTUController;
import ru.avem.ksptsurgut.utils.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static ru.avem.ksptsurgut.Constants.Measuring.HZ;
import static ru.avem.ksptsurgut.Constants.Measuring.VOLT;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.*;
import static ru.avem.ksptsurgut.communication.devices.deltaC2000.DeltaCP2000Controller.*;
import static ru.avem.ksptsurgut.communication.devices.ikas.IKASController.*;
import static ru.avem.ksptsurgut.communication.devices.phasemeter.PhaseMeterController.START_STOP_REGISTER;
import static ru.avem.ksptsurgut.communication.devices.pr200.OwenPRController.*;
import static ru.avem.ksptsurgut.utils.Utils.sleep;


public class CommunicationModel extends Observable implements Observer {
    public static final Object LOCK = new Object();

    private static CommunicationModel instance = new CommunicationModel();

    private Connection RS485Connection;
    private Connection CP2000Connection;

    public OwenPRController owenPRController;
    public PM130Controller pm130Controller;
    public PM130Controller pm130Controller2;
    public AvemVoltmeterController avemVoltmeterControllerA;
    public AvemVoltmeterController avemVoltmeterControllerB;
    public AvemVoltmeterController avemVoltmeterControllerC;
    public IKASController ikasController;
    public ParmaT400Controller parmaT400Controller;
    public PhaseMeterController phaseMeterController;
    public DeltaCP2000Controller deltaCP2000Controller;
    //    public FRA800Controller fra800ObjectController;
    public TRMController trmController;
    public CS02021Controller megacsController;

    private int kms1;
    private int kms2;
    private int kms3;

    private boolean lastOne;
    private boolean isFinished;

    private volatile boolean isDeviceStateOn;

    public List<DeviceController> devicesControllers = new ArrayList<>();

    private CommunicationModel() {

        connectMainBus();
        ModbusController modbusController = new RTUController(RS485Connection);

//        connectCP2000Bus();
//        ModbusController modbusCP2000 = new RTUController(CP2000Connection);

        pm130Controller = new PM130Controller(1, this, modbusController, PM130_ID);
        devicesControllers.add(pm130Controller);

        pm130Controller2 = new PM130Controller(2, this, modbusController, PM130_2_ID);
        devicesControllers.add(pm130Controller2);

        avemVoltmeterControllerA = new AvemVoltmeterController(13, this, modbusController, AVEM_A_ID);
        devicesControllers.add(avemVoltmeterControllerA);

        avemVoltmeterControllerB = new AvemVoltmeterController(14, this, modbusController, AVEM_B_ID);
        devicesControllers.add(avemVoltmeterControllerB);

        avemVoltmeterControllerC = new AvemVoltmeterController(15, this, modbusController, AVEM_C_ID);
        devicesControllers.add(avemVoltmeterControllerC);

        phaseMeterController = new PhaseMeterController(4, this, modbusController, PHASEMETER_ID);
        devicesControllers.add(phaseMeterController);

        ikasController = new IKASController(5, this, modbusController, IKAS_ID);
        devicesControllers.add(ikasController);

        owenPRController = new OwenPRController(6, this, modbusController, PR200_ID);
        devicesControllers.add(owenPRController);

        trmController = new TRMController(7, this, modbusController, TRM_ID);
        devicesControllers.add(trmController);

        megacsController = new CS02021Controller(MEGACS_ID, this, RS485Connection);
        devicesControllers.add(megacsController);

        deltaCP2000Controller = new DeltaCP2000Controller(11, this, modbusController, DELTACP2000_ID);
        devicesControllers.add(deltaCP2000Controller);

        parmaT400Controller = new ParmaT400Controller(12, this, modbusController, PARMA400_ID);
        devicesControllers.add(parmaT400Controller);


        new Thread(() -> {
            while (!isFinished) {
                for (DeviceController deviceController : devicesControllers) {
                    if (deviceController.isNeedToRead()) {
                        if (deviceController instanceof PM130Controller) {
                            for (int i = 1; i <= 4; i++) {
                                deviceController.read(i);
                            }
                        } else if (deviceController instanceof ParmaT400Controller) {
                            for (int i = 1; i <= 4; i++) {
                                deviceController.read(i);
                            }
                        } else {
                            deviceController.read();
                        }
                        if (deviceController instanceof OwenPRController) {
                            resetDog();
                        }
                    }
                    if (isDeviceStateOn) {
                        deviceController.resetAllDeviceStateOnAttempts();
                    }
                }
                sleep(1);
            }
        }).start();
    }

    public static CommunicationModel getInstance() {
        return instance;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    private void notice(int deviceID, int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }

    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);
        notice(modelId, param, value);
    }

    public void setNeedToReadAllDevices(boolean isNeed) {
        owenPRController.setNeedToRead(isNeed);
        megacsController.setNeedToRead(isNeed);
        deltaCP2000Controller.setNeedToRead(isNeed);
        ikasController.setNeedToRead(isNeed);
        parmaT400Controller.setNeedToRead(isNeed);
        phaseMeterController.setNeedToRead(isNeed);
        pm130Controller.setNeedToRead(isNeed);
        pm130Controller2.setNeedToRead(isNeed);
        trmController.setNeedToRead(isNeed);
        avemVoltmeterControllerA.setNeedToRead(isNeed);
        avemVoltmeterControllerB.setNeedToRead(isNeed);
        avemVoltmeterControllerC.setNeedToRead(isNeed);
    }

    public void setNeedToReadForDebug(boolean isNeed) {
        owenPRController.setNeedToRead(isNeed);
        deltaCP2000Controller.setNeedToRead(isNeed);
    }

    public void resetAllDevices() {
        owenPRController.resetAllAttempts();
//        megacsController.resetAllAttempts();
        deltaCP2000Controller.resetAllAttempts();
        ikasController.resetAllAttempts();
        parmaT400Controller.resetAllAttempts();
        phaseMeterController.resetAllAttempts();
        pm130Controller.resetAllAttempts();
        trmController.resetAllAttempts();
    }

    private void connectMainBus() {
        RS485Connection = new SerialConnection(
                Constants.Communication.RS485_DEVICE_NAME,
                Constants.Communication.BAUDRATE_MAIN,
                Constants.Communication.DATABITS,
                Constants.Communication.STOPBITS,
                Constants.Communication.PARITY,
                Constants.Communication.WRITE_TIMEOUT,
                Constants.Communication.READ_TIMEOUT);
        Logger.withTag("DEBUG_TAG").log("connectMainBus");
        if (!RS485Connection.isInitiatedConnection()) {
            Logger.withTag("DEBUG_TAG").log("!isInitiatedMainBus");
            RS485Connection.closeConnection();
            RS485Connection.initConnection();
        }
    }

    private void connectCP2000Bus() {
        CP2000Connection = new SerialConnection(
                Constants.Communication.CP2000_DEVICE_NAME,
                Constants.Communication.BAUDRATE_MAIN,
                Constants.Communication.DATABITS,
                Constants.Communication.STOPBITS,
                Constants.Communication.PARITY,
                Constants.Communication.WRITE_TIMEOUT,
                Constants.Communication.READ_TIMEOUT);
        Logger.withTag("DEBUG_TAG").log("connectMainBus");
        if (!CP2000Connection.isInitiatedConnection()) {
            Logger.withTag("DEBUG_TAG").log("!isInitiatedMainBus");
            CP2000Connection.closeConnection();
            CP2000Connection.initConnection();
        }
    }

    public void deinitPR() {
        owenPRController.write(RES_REGISTER, 1, 0);
    }

    public void finalizeAllDevices() {
        for (DeviceController deviceController : devicesControllers) {
            deviceController.setNeedToRead(false);
        }
    }

    public void finalizeMegaCS() {
        megacsController.setNeedToRead(false);
    }

    private void resetDog() {
        if (lastOne) {
            owenPRController.write(RESET_DOG, 1, 0);
            lastOne = false;
        } else {
            owenPRController.write(RESET_DOG, 1, 1);
            lastOne = true;
        }
    }

    private void resetTimer() {
        lastOne = true;

        owenPRController.write(RESET_DOG, 1, 0);
        owenPRController.write(RESET_DOG, 1, 1);
        owenPRController.write(RESET_TIMER, 1, 1);
        owenPRController.write(RESET_TIMER, 1, 0);
    }

    public void offAllKms() {
        kms1 = 0;
        writeToKms1Register(kms1);
        kms2 = 0;
        writeToKms2Register(kms2);
        kms2 = 0;
        writeToKms3Register(kms3);
    }

    public void onAllKms() {
        kms1 = 1;
        writeToKms1Register(kms1);
        kms2 = 2;
        writeToKms2Register(kms2);
        kms2 = 2;
        writeToKms3Register(kms3);
    }

    private void writeToKms1Register(int value) {
        owenPRController.write(KMS1_REGISTER, 1, value);
    }

    private void writeToKms2Register(int value) {
        owenPRController.write(KMS2_REGISTER, 1, value);
    }

    private void writeToKms3Register(int value) {
        owenPRController.write(KMS3_REGISTER, 1, value);
    }

    public void onRegisterInTheKms(int numberOfRegister, int kms) {
        int mask = (int) Math.pow(2, --numberOfRegister);
        try {
            int kmsField = CommunicationModel.class.getDeclaredField("kms" + kms).getInt(this);
            kmsField |= mask;
            CommunicationModel.class.getDeclaredMethod(String.format("%s%d%s", "writeToKms", kms, "Register"), int.class).invoke(this, kmsField);
            CommunicationModel.class.getDeclaredField("kms" + kms).set(this, kmsField);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
        }
        Logger.withTag("DEBUG_TAG").log("numberOfRegister=" + numberOfRegister + " kms=" + kms);
        Logger.withTag("DEBUG_TAG").log("1=" + kms1 + " 2=" + kms2);
    }

    public void offRegisterInTheKms(int numberOfRegister, int kms) {
        int mask = ~(int) Math.pow(2, --numberOfRegister);
        try {
            int kmsField = CommunicationModel.class.getDeclaredField("kms" + kms).getInt(this);
            kmsField &= mask;
            CommunicationModel.class.getDeclaredMethod(String.format("%s%d%s", "writeToKms", kms, "Register"), int.class).invoke(this, kmsField);
            CommunicationModel.class.getDeclaredField("kms" + kms).set(this, kmsField);
        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
        }
        Logger.withTag("DEBUG_TAG").log("numberOfRegister=" + numberOfRegister + " kms=" + kms);
        Logger.withTag("DEBUG_TAG").log("1=" + kms1 + " 2=" + kms2);
    }

    public void initOwenPrController() {
        owenPRController.resetAllAttempts();
        owenPRController.setNeedToRead(true);
        offAllKms();
        resetTimer();
        owenPRController.write(RES_REGISTER, 1, 1);
    }

    public void startPhaseMeter() {
        phaseMeterController.write(START_STOP_REGISTER, (short) 0x01);
    }

    public void startMeasuringAB() {
        ikasController.write(MEASURABLE_TYPE_REGISTER, MEASURABLE_TYPE_AB);
        sleep(1 * 1000);
        ikasController.write(START_MEASURABLE_REGISTER, 0x01);
    }

    public void startMeasuringBC() {
        ikasController.write(MEASURABLE_TYPE_REGISTER, MEASURABLE_TYPE_BC);
        sleep(1 * 1000);
        ikasController.write(START_MEASURABLE_REGISTER, 0x01);
    }

    public void startMeasuringAC() {
        ikasController.write(MEASURABLE_TYPE_REGISTER, MEASURABLE_TYPE_AC);
        sleep(1 * 1000);
        ikasController.write(START_MEASURABLE_REGISTER, 0x01);
    }

    public void startObject() {
        deltaCP2000Controller.write(CONTROL_REGISTER, 1, 0b10);
    }

    public void startReversObject() {
        deltaCP2000Controller.write(CONTROL_REGISTER, 1, 0b10_00_10);
    }

    public void stopObject() {
        deltaCP2000Controller.write(CONTROL_REGISTER, 1, 0b1);
    }

    public void setObjectParams(int fOut, int voltageP1, int fP1) {
        deltaCP2000Controller.write(MAX_VOLTAGE_REGISTER, 1, 400 * VOLT);
        deltaCP2000Controller.write(MAX_FREQUENCY_REGISTER, 1, 210 * HZ);
        deltaCP2000Controller.write(NOM_FREQUENCY_REGISTER, 1, 210 * HZ);
        deltaCP2000Controller.write(CURRENT_FREQUENCY_OUTPUT_REGISTER, 1, fOut);
        deltaCP2000Controller.write(POINT_1_VOLTAGE_REGISTER, 1, voltageP1);
        deltaCP2000Controller.write(POINT_1_FREQUENCY_REGISTER, 1, fP1);
        deltaCP2000Controller.write(POINT_2_VOLTAGE_REGISTER, 1, 40);
        deltaCP2000Controller.write(POINT_2_FREQUENCY_REGISTER, 1, 50);
    }

    public void setObjectFCur(int fCur) {
        deltaCP2000Controller.write(CURRENT_FREQUENCY_OUTPUT_REGISTER, 1, fCur);
    }

    public void setObjectUMax(int voltageMax) {
        deltaCP2000Controller.write(POINT_1_VOLTAGE_REGISTER, 1, voltageMax);
    }


    public boolean setUMgr(int u) {
        setCS02021ExperimentRun(true);
        return megacsController.setVoltage(u);
    }

    public float[] readDataMgr() {
        return megacsController.readData();
    }

    public void setCS02021ExperimentRun(boolean b) {
        megacsController.setExperimentRun(b);
    }

    public void initExperiment1Devices() {
    }

    public void initExperiment2Devices() {
        resetTimer();
        ikasController.setNeedToRead(true);
        ikasController.resetAllAttempts();
        trmController.setNeedToRead(true);
        trmController.resetAllAttempts();
    }

    public void initExperiment3Devices() {
        resetTimer();
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        pm130Controller2.setNeedToRead(true);
        pm130Controller2.resetAllAttempts();
        phaseMeterController.setNeedToRead(true);
        phaseMeterController.resetAllAttempts();
//        deltaCP2000Controller.setNeedToRead(true);
//        deltaCP2000Controller.resetAllAttempts();
    }

    public void initExperiment4Devices() {
        resetTimer();
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        deltaCP2000Controller.setNeedToRead(true);
        deltaCP2000Controller.resetAllAttempts();
        avemVoltmeterControllerA.setNeedToRead(true);
        avemVoltmeterControllerA.resetAllAttempts();
        avemVoltmeterControllerB.setNeedToRead(true);
        avemVoltmeterControllerB.resetAllAttempts();
        avemVoltmeterControllerC.setNeedToRead(true);
        avemVoltmeterControllerC.resetAllAttempts();
        parmaT400Controller.setNeedToRead(true);
        parmaT400Controller.resetAllAttempts();
    }

    public void initExperiment5Devices() {
        resetTimer();
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
    }

    public void initExperiment6Devices() {
        resetTimer();
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        deltaCP2000Controller.setNeedToRead(true);
        deltaCP2000Controller.resetAllAttempts();
    }

    public void initExperiment7Devices() {
        resetTimer();
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        deltaCP2000Controller.setNeedToRead(true);
        deltaCP2000Controller.resetAllAttempts();
    }

    public void onKM1() {
        onRegisterInTheKms(1, 1);
    }

    public void onKM2() {
        onRegisterInTheKms(2, 1);
    }

    public void onKM3() {
        onRegisterInTheKms(3, 1);
    }

    public void onKM11() {
        onRegisterInTheKms(4, 1);
    }

    public void onKM10() {
        onRegisterInTheKms(5, 1);
    }

    public void onKM15() {
        onRegisterInTheKms(6, 1);
    }

    public void onKM14() {
        onRegisterInTheKms(7, 1);
    }

    public void onKM1213() {
        onRegisterInTheKms(8, 1);
    }

    public void onKM24() {
        onRegisterInTheKms(1, 2);
    }

    public void onKM69() {
        onRegisterInTheKms(2, 2);
    }

    public void onKM58() {
        onRegisterInTheKms(3, 2);
    }

    public void onKM47() {
        onRegisterInTheKms(4, 2);
    }

    public void onKM22() {
        onRegisterInTheKms(5, 2);
    }

    public void onKM21() {
        onRegisterInTheKms(6, 2);
    }

    public void onKM20() {
        onRegisterInTheKms(7, 2);
    }

    public void onKM19() {
        onRegisterInTheKms(8, 2);
    }

    public void onKM16() {
        onRegisterInTheKms(1, 3);
    }

    public void onKM17() {
        onRegisterInTheKms(2, 3);
    }

    public void onKM18() {
        onRegisterInTheKms(3, 3);
    }

    public void onKM27() {
        onRegisterInTheKms(4, 3);
    }

    public void onKM25() {
        onRegisterInTheKms(5, 3);
    }

    public void onDOM6() {
        onRegisterInTheKms(6, 3);
    }

    public void onLight() {
        onRegisterInTheKms(7, 3);
    }

    public void onSound() {
        onRegisterInTheKms(8, 3);
    }

    public void offDO1() {
        offRegisterInTheKms(1, 1);
    }

    public void offDO2() {
        offRegisterInTheKms(2, 1);
    }

    public void offDO3() {
        offRegisterInTheKms(3, 1);
    }

    public void offDO4() {
        offRegisterInTheKms(4, 1);
    }

    public void offDO5() {
        offRegisterInTheKms(5, 1);
    }

    public void offDO6() {
        offRegisterInTheKms(6, 1);
    }

    public void offDO7() {
        offRegisterInTheKms(7, 1);
    }

    public void offDO8() {
        offRegisterInTheKms(8, 1);
    }

    public void offDO9() {
        offRegisterInTheKms(1, 2);
    }

    public void offDO10() {
        offRegisterInTheKms(2, 2);
    }

    public void offDO11() {
        offRegisterInTheKms(3, 2);
    }

    public void offDO12() {
        offRegisterInTheKms(4, 2);
    }

    public void offDO13() {
        offRegisterInTheKms(5, 2);
    }

    public void offDO14() {
        offRegisterInTheKms(6, 2);
    }

    public void offDO15() {
        offRegisterInTheKms(7, 2);
    }

    public void offDO16() {
        offRegisterInTheKms(8, 2);
    }

    public void offDOM1() {
        offRegisterInTheKms(1, 3);
    }

    public void offDOM2() {
        offRegisterInTheKms(2, 3);
    }

    public void offDOM3() {
        offRegisterInTheKms(3, 3);
    }

    public void offDOM4() {
        offRegisterInTheKms(4, 3);
    }

    public void offDOM5() {
        offRegisterInTheKms(5, 3);
    }

    public void offDOM6() {
        offRegisterInTheKms(6, 3);
    }

    public void offDOM7() {
        offRegisterInTheKms(7, 3);
    }

    public void offDOM8() {
        offRegisterInTheKms(8, 3);
    }


    public void setDeviceStateOn(boolean deviceStateOn) {
        isDeviceStateOn = deviceStateOn;
    }
}
