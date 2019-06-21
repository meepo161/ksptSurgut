package ru.avem.ksptamur.communication;

import ru.avem.ksptamur.Constants;
import ru.avem.ksptamur.communication.connections.Connection;
import ru.avem.ksptamur.communication.connections.SerialConnection;
import ru.avem.ksptamur.communication.devices.DeviceController;
import ru.avem.ksptamur.communication.devices.cs02021.CS02021Controller;
import ru.avem.ksptamur.communication.devices.deltaC2000.DeltaCP2000Controller;
import ru.avem.ksptamur.communication.devices.ikas.IKASController;
import ru.avem.ksptamur.communication.devices.parmaT400.ParmaT400Controller;
import ru.avem.ksptamur.communication.devices.phasemeter.PhaseMeterController;
import ru.avem.ksptamur.communication.devices.pm130.PM130Controller;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRController;
import ru.avem.ksptamur.communication.devices.trm.TRMController;
import ru.avem.ksptamur.communication.modbus.ModbusController;
import ru.avem.ksptamur.communication.modbus.RTUController;
import ru.avem.ksptamur.utils.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static ru.avem.ksptamur.Constants.Measuring.HZ;
import static ru.avem.ksptamur.Constants.Measuring.VOLT;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.communication.devices.deltaC2000.DeltaCP2000Controller.*;
import static ru.avem.ksptamur.communication.devices.ikas.IKASController.*;
import static ru.avem.ksptamur.communication.devices.phasemeter.PhaseMeterController.START_STOP_REGISTER;
import static ru.avem.ksptamur.communication.devices.pr200.OwenPRController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;


public class CommunicationModel extends Observable implements Observer {

    private static CommunicationModel instance = new CommunicationModel();

    private Connection RS485Connection;

    public OwenPRController owenPRController;
    public PM130Controller pm130Controller;
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

        pm130Controller = new PM130Controller(1, this, modbusController, PM130_ID);
        devicesControllers.add(pm130Controller);

        parmaT400Controller = new ParmaT400Controller(2, this, modbusController, PARMA400_ID);
        devicesControllers.add(parmaT400Controller);

        phaseMeterController = new PhaseMeterController(4, this, modbusController, PHASEMETER_ID);
        devicesControllers.add(phaseMeterController);

        ikasController = new IKASController(5, this, modbusController, IKAS_ID);
        devicesControllers.add(ikasController);

        owenPRController = new OwenPRController(6, this, modbusController, PR200_ID);
        devicesControllers.add(owenPRController);

        trmController = new TRMController(7, this, modbusController, TRM_ID);
        devicesControllers.add(trmController);

        megacsController = new CS02021Controller(8, this, RS485Connection, MEGACS_ID);
        devicesControllers.add(megacsController);

        deltaCP2000Controller = new DeltaCP2000Controller(11, this, modbusController, DELTACP2000_ID);
        devicesControllers.add(deltaCP2000Controller);

        new Thread(() -> {
            while (!isFinished) {
                for (DeviceController deviceController : devicesControllers) {
                    if (deviceController.needToRead()) {
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
        trmController.setNeedToRead(isNeed);
    }

    public void resetAllDevices() {
        owenPRController.resetAllAttempts();
        megacsController.resetAllAttempts();
        deltaCP2000Controller.resetAllAttempts();
        ikasController.resetAllAttempts();
        parmaT400Controller.resetAllAttempts();
        phaseMeterController.resetAllAttempts();
        pm130Controller.resetAllAttempts();
        trmController.resetAllAttempts();
    }

    public void connectMainBus() {
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
            RS485Connection.initConnection();
        }
    }

    public void setConnectionBaudrate(int baudrate) {
        RS485Connection.setPortParameters(
                baudrate,
                Constants.Communication.DATABITS,
                Constants.Communication.STOPBITS,
                Constants.Communication.PARITY);
    }

    public void finalizeAllDevices() {
        owenPRController.write(RES_REGISTER, 1, 0);
        offAllKms();
        for (DeviceController deviceController : devicesControllers) {
            deviceController.setNeedToRead(false);
        }
    }

    public void finalizeMegaCS() {
        megacsController.setNeedToRead(false);
    }

    private void resetDog() {
        if (lastOne) {
            Logger.withTag("StatusActivity").log("Dog off");
            owenPRController.write(RESET_DOG, 1, 0);
            lastOne = false;
        } else {
            Logger.withTag("StatusActivity").log("Dog on");
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
        kms3 = 0;
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
        Logger.withTag("DEBUG_TAG").log("1=" + kms1 + " 2=" + kms2 + " 3=" + kms3);
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
        Logger.withTag("DEBUG_TAG").log("1=" + kms1 + " 2=" + kms2 + " 3=" + kms3);
    }

    public void initOwenPrController() {
        owenPRController.setNeedToRead(true);
        offAllKms();
        owenPRController.resetAllAttempts();
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

    public void initExperiment0Devices() {
        megacsController.setNeedToRead(true);
    }

    public void initExperiment1Devices() {
        ikasController.setNeedToRead(true);
        ikasController.resetAllAttempts();
        trmController.setNeedToRead(true);
        trmController.resetAllAttempts();
    }

    public void initExperiment2Devices() {
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        parmaT400Controller.setNeedToRead(true);
        parmaT400Controller.resetAllAttempts();
    }

    public void initExperiment3Devices() {
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        parmaT400Controller.setNeedToRead(true);
        parmaT400Controller.resetAllAttempts();
        phaseMeterController.setNeedToRead(true);
        phaseMeterController.resetAllAttempts();
    }

    public void initExperiment4Devices() {
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        deltaCP2000Controller.setNeedToRead(true);
        deltaCP2000Controller.resetAllAttempts();
    }

    public void initExperiment5Devices() {
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
    }

    public void initExperiment6Devices() {
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        deltaCP2000Controller.setNeedToRead(true);
        deltaCP2000Controller.resetAllAttempts();
    }

    public void initExperiment7Devices() {
    }

    public void initExperiment8Devices() {
    }

    public void onKM2() {
        onRegisterInTheKms(1, 1);
    }

    public void onKM3() {
        onRegisterInTheKms(2, 1);
    }

    public void onKM4() {
        onRegisterInTheKms(3, 1);
    }

    public void onKM5() {
        onRegisterInTheKms(4, 1);
    }

    public void onKM6() {
        onRegisterInTheKms(5, 1);
    }

    public void onKM7() {
        onRegisterInTheKms(6, 1);
    }

    public void onKM11() {
        onRegisterInTheKms(7, 1);
    }

    public void onKM12() {
        onRegisterInTheKms(8, 1);
    }

    public void onKM13() {
        onRegisterInTheKms(1, 2);
    }

    public void onKM17() {
        onRegisterInTheKms(2, 2);
    }

    public void onPR3M1() {
        onRegisterInTheKms(3, 2);
    }

    public void onPR4M1() {
        onRegisterInTheKms(4, 2);
    }

    public void onPR5M1() {
        onRegisterInTheKms(5, 2);
    }

    public void onPR6M1() {
        onRegisterInTheKms(6, 2);
    }

    public void onPR7M1() {
        onRegisterInTheKms(7, 2);
    }

    public void onPR8M1() {
        onRegisterInTheKms(8, 2);
    }


    public void offPR1() {
        offRegisterInTheKms(1, 1);
    }

    public void offPR2() {
        offRegisterInTheKms(2, 1);
    }

    public void offPR3() {
        offRegisterInTheKms(3, 1);
    }

    public void offPR4() {
        offRegisterInTheKms(4, 1);
    }

    public void offPR5() {
        offRegisterInTheKms(5, 1);
    }

    public void offPR6() {
        offRegisterInTheKms(6, 1);
    }

    public void offPR7() {
        offRegisterInTheKms(7, 1);
    }

    public void offPR8() {
        offRegisterInTheKms(8, 1);
    }

    public void offKM1M1() {
        offRegisterInTheKms(1, 2);
    }

    public void offPR2M1() {
        offRegisterInTheKms(2, 2);
    }

    public void offPR3M1() {
        offRegisterInTheKms(3, 2);
    }

    public void offPR4M1() {
        offRegisterInTheKms(4, 2);
    }

    public void offPR5M1() {
        offRegisterInTheKms(5, 2);
    }

    public void offPR6M1() {
        offRegisterInTheKms(6, 2);
    }

    public void offPR7M1() {
        offRegisterInTheKms(7, 2);
    }

    public void offPR8M1() {
        offRegisterInTheKms(8, 2);
    }

    public void setDeviceStateOn(boolean deviceStateOn) {
        isDeviceStateOn = deviceStateOn;
    }
}
