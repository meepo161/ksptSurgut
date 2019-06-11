package ru.avem.ksptamur.logging;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.avem.ksptamur.Main;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.utils.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static ru.avem.ksptamur.utils.Utils.copyFileFromStream;

public class Logging {


    public static File getTempWorkbook(Protocol protocol) {
        if (protocol == null) {
            return null;
        }
        return writeWorkbookToTempFile(protocol);
    }

    private static File writeWorkbookToTempFile(Protocol protocol) {
        clearDirectory();
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM(HH-mm-ss)");
        String fileName = "protocol-" + sdf.format(System.currentTimeMillis()) + ".xlsx";

        File file = new File("protocol", fileName);
        if (!writeWorkbookToFile(protocol, file)) {
            Toast.makeText("Произошла ошибка при попытке отображения протокола").show(Toast.ToastType.ERROR);
        }
        return file;
    }

    private static void clearDirectory() {
        File directory = new File("protocol");
        if (!directory.exists()) {
            directory.mkdir();
        } else if (directory.listFiles() != null) {
            for (File child : directory.listFiles()) {
                child.delete();
            }
        }
    }

    public static boolean writeWorkbookToFile(Protocol protocol, File file) {
        try {
            ByteArrayOutputStream out;

            switch ((int) protocol.getPhase()) {
                case 1:
                    out = convertProtocolToWorkbookPhase1(protocol);
                    break;
                case 3:
                    out = convertProtocolToWorkbookPhase3(protocol);
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            FileOutputStream fileOut = new FileOutputStream(file);
            out.writeTo(fileOut);
            out.close();
            fileOut.close();
        } catch (IOException | InvalidFormatException e) {
            return false;
        }
        return true;
    }

    private static ByteArrayOutputStream convertProtocolToWorkbookPhase1(Protocol protocol) throws IOException, InvalidFormatException {
        File templateTempFile = new File(System.getProperty("user.dir"), "tmp.xlsx");
        copyFileFromStream(Main.class.getResourceAsStream("raw/template_phase1.xlsx"), templateTempFile);
        try (Workbook wb = new XSSFWorkbook(templateTempFile)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 0; i < 100; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < 20; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null && (cell.getCellTypeEnum() == CellType.STRING)) {
                            switch (cell.getStringCellValue()) {
                                case "$PROTOCOL_NUMBER$":
                                    long id = protocol.getId();
                                    if (id != 0) {
                                        cell.setCellValue(id + "");
                                    } else {
                                        cell.setCellValue("");
                                    }
                                    break;
                                case "$OBJECT$":
                                    String objectName = protocol.getType();
                                    if (objectName != null) {
                                        cell.setCellValue(objectName);
                                    } else {
                                        cell.setCellValue("");
                                    }
                                    break;
                                case "$SERIAL_NUMBER$":
                                    String serialNumber = protocol.getSerialNumber();
                                    if ((serialNumber != null) && !serialNumber.isEmpty()) {
                                        cell.setCellValue(serialNumber);
                                    } else {
                                        cell.setCellValue("");
                                    }
                                    break;
                                case "$1$":
                                    cell.setCellValue(protocol.getE1WindingBH());
                                    break;
                                case "$2$":
                                    cell.setCellValue(protocol.getE1ABBH());
                                    break;
                                case "$3$":
                                    cell.setCellValue(protocol.getE1TBH());
                                    break;
                                case "$4$":
                                    cell.setCellValue(protocol.getE1ResultBH());
                                    break;
                                case "$5$":
                                    cell.setCellValue(protocol.getE1WindingHH());
                                    break;
                                case "$6$":
                                    cell.setCellValue(protocol.getE1ABHH());
                                    break;
                                case "$7$":
                                    cell.setCellValue(protocol.getE1THH());
                                    break;
                                case "$8$":
                                    cell.setCellValue(protocol.getE1ResultHH());
                                    break;
                                case "$9$":
                                    cell.setCellValue(protocol.getE2UInputAB());
                                    break;
                                case "$10$":
                                    cell.setCellValue(protocol.getE2UOutputAB());
                                    break;
                                case "$11$":
                                    cell.setCellValue(protocol.getE2DiffU());
                                    break;
                                case "$12$":
                                    cell.setCellValue(protocol.getE2F());
                                    break;
                                case "$13$":
                                    cell.setCellValue(protocol.getE2Result());
                                    break;
                                case "$14$":
                                    cell.setCellValue(protocol.getE3UBH());
                                    break;
                                case "$15$":
                                    cell.setCellValue(protocol.getE3UHH());
                                    break;
                                case "$16$":
                                    cell.setCellValue(protocol.getE3F());
                                    break;
                                case "$17$":
                                    cell.setCellValue(protocol.getE3Result());
                                    break;
                                case "$18$":
                                    cell.setCellValue(protocol.getE4WindingBH());
                                    break;
                                case "$19$":
                                    cell.setCellValue(protocol.getE4WindingHH());
                                    break;
                                case "$20$":
                                    cell.setCellValue(protocol.getE4UBH());
                                    break;
                                case "$21$":
                                    cell.setCellValue(protocol.getE4UHH());
                                    break;
                                case "$22$":
                                    cell.setCellValue(protocol.getE4Result());
                                    break;
                                case "$23$":
                                    cell.setCellValue(protocol.getE5UKZV());
                                    break;
                                case "$24$":
                                    cell.setCellValue(protocol.getE5UKZPercent());
                                    break;
                                case "$124$":
                                    cell.setCellValue(protocol.getE5UKZDiff());
                                    break;
                                case "$25$":
                                    cell.setCellValue(protocol.getE5IA());
                                    break;
                                case "$26$":
                                    cell.setCellValue(protocol.getE5Pp());
                                    break;
                                case "$27$":
                                    cell.setCellValue(protocol.getE5F());
                                    break;
                                case "$28$":
                                    cell.setCellValue(protocol.getE5Result());
                                    break;
                                case "$29$":
                                    cell.setCellValue(protocol.getE6UBH());
                                    break;
                                case "$30$":
                                    cell.setCellValue(protocol.getE6IA());
                                    break;
                                case "$130$":
                                    cell.setCellValue(protocol.getE6IADiff());
                                    break;
                                case "$31$":
                                    cell.setCellValue(protocol.getE6Pp());
                                    break;
                                case "$32$":
                                    cell.setCellValue(protocol.getE6Cos());
                                    break;
                                case "$33$":
                                    cell.setCellValue(protocol.getE6F());
                                    break;
                                case "$34$":
                                    cell.setCellValue(protocol.getE6Result());
                                    break;
                                case "$35$":
                                    cell.setCellValue(protocol.getE7UInput());
                                    break;
                                case "$36$":
                                    cell.setCellValue(protocol.getE7IBH());
                                    break;
                                case "$37$":
                                    cell.setCellValue(protocol.getE7F());
                                    break;
                                case "$38$":
                                    cell.setCellValue(protocol.getE7Time());
                                    break;
                                case "$39$":
                                    cell.setCellValue(protocol.getE7Result());
                                    break;
                                case "$40$":
                                    cell.setCellValue(protocol.getE8TypeBHandCorps());
                                    break;
                                case "$41$":
                                    cell.setCellValue(protocol.getE8UBHandCorps());
                                    break;
                                case "$42$":
                                    cell.setCellValue(protocol.getE8IBHandCorps());
                                    break;
                                case "$43$":
                                    cell.setCellValue(protocol.getE8TimeBHandCorps());
                                    break;
                                case "$44$":
                                    cell.setCellValue(protocol.getE8ResultBHandCorps());
                                    break;
                                case "$45$":
                                    cell.setCellValue(protocol.getE8TypeHHandCorps());
                                    break;
                                case "$46$":
                                    cell.setCellValue(protocol.getE8IHHandCorps());
                                    break;
                                case "$47$":
                                    cell.setCellValue(protocol.getE8UHHandCorps());
                                    break;
                                case "$48$":
                                    cell.setCellValue(protocol.getE8TimeHHandCorps());
                                    break;
                                case "$49$":
                                    cell.setCellValue(protocol.getE8ResultHHandCorps());
                                    break;
                                case "$101$":
                                    cell.setCellValue(protocol.getType());
                                    break;
                                case "$102$":
                                    cell.setCellValue(protocol.getUbh());
                                    break;
                                case "$103$":
                                    cell.setCellValue(protocol.getUhh());
                                    break;
                                case "$104$":
                                    cell.setCellValue(protocol.getP());
                                    break;
                                case "$105$":
                                    cell.setCellValue(protocol.getPhase());
                                    break;
                                case "$106$":
                                    cell.setCellValue(protocol.getIxx());
                                    break;
                                case "$107$":
                                    cell.setCellValue(protocol.getUkz());
                                    break;
                                case "$108$":
                                    cell.setCellValue(protocol.getXxtime());
                                    break;
                                case "$109$":
                                    cell.setCellValue(protocol.getUinsulation());
                                    break;
                                case "$POS1$":
                                    cell.setCellValue(protocol.getPosition1());
                                    break;
                                case "$POS2$":
                                    cell.setCellValue(protocol.getPosition2());
                                    break;
                                case "$POS1NAME$":
                                    cell.setCellValue(String.format("/%s/", protocol.getPosition1FullName()));
                                    break;
                                case "$POS2NAME$":
                                    cell.setCellValue(String.format("/%s/", protocol.getPosition2FullName()));
                                    break;
                                case "$DATE$":
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
                                    cell.setCellValue(sdf.format(protocol.getMillis()));
                                    break;
                                default:
                                    if (cell.getStringCellValue().contains("$")) {
                                        cell.setCellValue("");
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                wb.write(out);
            } finally {
                out.close();
            }
            return out;
        }
    }


    private static ByteArrayOutputStream convertProtocolToWorkbookPhase3(Protocol protocol) throws IOException, InvalidFormatException {
        File templateTempFile = new File(System.getProperty("user.dir"), "tmp.xlsx");
        copyFileFromStream(Main.class.getResourceAsStream("raw/template_phase3.xlsx"), templateTempFile);
        try (Workbook wb = new XSSFWorkbook(templateTempFile)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 0; i < 100; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < 20; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null && (cell.getCellTypeEnum() == CellType.STRING)) {
                            switch (cell.getStringCellValue()) {
                                case "$PROTOCOL_NUMBER$":
                                    long id = protocol.getId();
                                    if (id != 0) {
                                        cell.setCellValue(id + "");
                                    } else {
                                        cell.setCellValue("");
                                    }
                                    break;
                                case "$OBJECT$":
                                    String objectName = protocol.getType();
                                    if (objectName != null) {
                                        cell.setCellValue(objectName);
                                    } else {
                                        cell.setCellValue("");
                                    }
                                    break;
                                case "$SERIAL_NUMBER$":
                                    String serialNumber = protocol.getSerialNumber();
                                    if ((serialNumber != null) && !serialNumber.isEmpty()) {
                                        cell.setCellValue(serialNumber);
                                    } else {
                                        cell.setCellValue("");
                                    }
                                    break;
                                case "$201$":
                                    cell.setCellValue(protocol.getE0UBH());
                                    break;
                                case "$202$":
                                    cell.setCellValue(protocol.getE0R15BH());
                                    break;
                                case "$203$":
                                    cell.setCellValue(protocol.getE0R60BH());
                                    break;
                                case "$204$":
                                    cell.setCellValue(protocol.getE0CoefBH());
                                    break;
                                case "$205$":
                                    cell.setCellValue(protocol.getE0ResultBH());
                                    break;
                                case "$206$":
                                    cell.setCellValue(protocol.getE0UHH());
                                    break;
                                case "$207$":
                                    cell.setCellValue(protocol.getE0R15HH());
                                    break;
                                case "$208$":
                                    cell.setCellValue(protocol.getE0R60HH());
                                    break;
                                case "$209$":
                                    cell.setCellValue(protocol.getE0CoefHH());
                                    break;
                                case "$210$":
                                    cell.setCellValue(protocol.getE0ResultHH());
                                    break;
                                case "$211$":
                                    cell.setCellValue(protocol.getE0UBHHH());
                                    break;
                                case "$212$":
                                    cell.setCellValue(protocol.getE0R15BHHH());
                                    break;
                                case "$213$":
                                    cell.setCellValue(protocol.getE0R60BHHH());
                                    break;
                                case "$214$":
                                    cell.setCellValue(protocol.getE0CoefBHHH());
                                    break;
                                case "$215$":
                                    cell.setCellValue(protocol.getE0ResultBHHH());
                                    break;
                                case "$1$":
                                    cell.setCellValue(protocol.getE1WindingBH());
                                    break;
                                case "$2$":
                                    cell.setCellValue(protocol.getE1ABBH());
                                    break;
                                case "$3$":
                                    cell.setCellValue(protocol.getE1BCBH());
                                    break;
                                case "$4$":
                                    cell.setCellValue(protocol.getE1CABH());
                                    break;
                                case "$5$":
                                    cell.setCellValue(protocol.getE1TBH());
                                    break;
                                case "$6$":
                                    cell.setCellValue(protocol.getE1ResultBH());
                                    break;
                                case "$7$":
                                    cell.setCellValue(protocol.getE1WindingHH());
                                    break;
                                case "$8$":
                                    cell.setCellValue(protocol.getE1ABHH());
                                    break;
                                case "$9$":
                                    cell.setCellValue(protocol.getE1BCHH());
                                    break;
                                case "$10$":
                                    cell.setCellValue(protocol.getE1CAHH());
                                    break;
                                case "$11$":
                                    cell.setCellValue(protocol.getE1THH());
                                    break;
                                case "$12$":
                                    cell.setCellValue(protocol.getE1ResultHH());
                                    break;
                                case "$13$":
                                    cell.setCellValue(protocol.getE2UInputAB());
                                    break;
                                case "$14$":
                                    cell.setCellValue(protocol.getE2UInputBC());
                                    break;
                                case "$15$":
                                    cell.setCellValue(protocol.getE2UInputCA());
                                    break;
                                case "$16$":
                                    cell.setCellValue(protocol.getE2UInputAvr());
                                    break;
                                case "$17$":
                                    cell.setCellValue(protocol.getE2UOutputAB());
                                    break;
                                case "$18$":
                                    cell.setCellValue(protocol.getE2UOutputBC());
                                    break;
                                case "$19$":
                                    cell.setCellValue(protocol.getE2UOutputCA());
                                    break;
                                case "$20$":
                                    cell.setCellValue(protocol.getE2UOutputAvr());
                                    break;
                                case "$21$":
                                    cell.setCellValue(protocol.getE2DiffU());
                                    break;
                                case "$22$":
                                    cell.setCellValue(protocol.getE2F());
                                    break;
                                case "$23$":
                                    cell.setCellValue(protocol.getE2Result());
                                    break;
                                case "$24$":
                                    cell.setCellValue(protocol.getE3UBH());
                                    break;
                                case "$25$":
                                    cell.setCellValue(protocol.getE3UHH());
                                    break;
                                case "$26$":
                                    cell.setCellValue(protocol.getE3F());
                                    break;
                                case "$27$":
                                    cell.setCellValue(protocol.getE3Result());
                                    break;
                                case "$28$":
                                    cell.setCellValue(protocol.getE4WindingBH());
                                    break;
                                case "$29$":
                                    cell.setCellValue(protocol.getE4WindingHH());
                                    break;
                                case "$30$":
                                    cell.setCellValue(protocol.getE4UBH());
                                    break;
                                case "$31$":
                                    cell.setCellValue(protocol.getE4UHH());
                                    break;
                                case "$32$":
                                    cell.setCellValue(protocol.getE4Result());
                                    break;
                                case "$33$":
                                    cell.setCellValue(protocol.getE5UKZV());
                                    break;
                                case "$34$":
                                    cell.setCellValue(protocol.getE5UKZPercent());
                                    break;
                                case "$134$":
                                    cell.setCellValue(protocol.getE5UKZDiff());
                                    break;
                                case "$35$":
                                    cell.setCellValue(protocol.getE5IA());
                                    break;
                                case "$36$":
                                    cell.setCellValue(protocol.getE5IB());
                                    break;
                                case "$37$":
                                    cell.setCellValue(protocol.getE5IC());
                                    break;
                                case "$38$":
                                    cell.setCellValue(protocol.getE5Pp());
                                    break;
                                case "$39$":
                                    cell.setCellValue(protocol.getE5F());
                                    break;
                                case "$40$":
                                    cell.setCellValue(protocol.getE5Result());
                                    break;
                                case "$41$":
                                    cell.setCellValue(protocol.getE6UBH());
                                    break;
                                case "$42$":
                                    cell.setCellValue(protocol.getE6IA());
                                    break;
                                case "$43$":
                                    cell.setCellValue(protocol.getE6IB());
                                    break;
                                case "$44$":
                                    cell.setCellValue(protocol.getE6IC());
                                    break;
                                case "$142$":
                                    cell.setCellValue(protocol.getE6IADiff());
                                    break;
                                case "$143$":
                                    cell.setCellValue(protocol.getE6IBDiff());
                                    break;
                                case "$144$":
                                    cell.setCellValue(protocol.getE6ICDiff());
                                    break;
                                case "$45$":
                                    cell.setCellValue(protocol.getE6Pp());
                                    break;
                                case "$46$":
                                    cell.setCellValue(protocol.getE6Cos());
                                    break;
                                case "$47$":
                                    cell.setCellValue(protocol.getE6F());
                                    break;
                                case "$48$":
                                    cell.setCellValue(protocol.getE6Result());
                                    break;
                                case "$49$":
                                    cell.setCellValue(protocol.getE7UInput());
                                    break;
                                case "$50$":
                                    cell.setCellValue(protocol.getE7IBH());
                                    break;
                                case "$51$":
                                    cell.setCellValue(protocol.getE7F());
                                    break;
                                case "$52$":
                                    cell.setCellValue(protocol.getE7Time());
                                    break;
                                case "$53$":
                                    cell.setCellValue(protocol.getE7Result());
                                    break;
                                case "$54$":
                                    cell.setCellValue(protocol.getE8TypeBHandCorps());
                                    break;
                                case "$55$":
                                    cell.setCellValue(protocol.getE8UBHandCorps());
                                    break;
                                case "$56$":
                                    cell.setCellValue(protocol.getE8IBHandCorps());
                                    break;
                                case "$57$":
                                    cell.setCellValue(protocol.getE8TimeBHandCorps());
                                    break;
                                case "$58$":
                                    cell.setCellValue(protocol.getE8ResultBHandCorps());
                                    break;
                                case "$59$":
                                    cell.setCellValue(protocol.getE8TypeHHandCorps());
                                    break;
                                case "$60$":
                                    cell.setCellValue(protocol.getE8UHHandCorps());
                                    break;
                                case "$61$":
                                    cell.setCellValue(protocol.getE8IHHandCorps());
                                    break;
                                case "$62$":
                                    cell.setCellValue(protocol.getE8TimeHHandCorps());
                                    break;
                                case "$63$":
                                    cell.setCellValue(protocol.getE8ResultHHandCorps());
                                    break;
                                case "$101$":
                                    cell.setCellValue(protocol.getType());
                                    break;
                                case "$102$":
                                    cell.setCellValue(protocol.getUbh());
                                    break;
                                case "$103$":
                                    cell.setCellValue(protocol.getUhh());
                                    break;
                                case "$104$":
                                    cell.setCellValue(protocol.getP());
                                    break;
                                case "$105$":
                                    cell.setCellValue(protocol.getPhase());
                                    break;
                                case "$106$":
                                    cell.setCellValue(protocol.getIxx());
                                    break;
                                case "$107$":
                                    cell.setCellValue(protocol.getUkz());
                                    break;
                                case "$108$":
                                    cell.setCellValue(protocol.getXxtime());
                                    break;
                                case "$109$":
                                    cell.setCellValue(protocol.getUinsulation());
                                    break;
                                case "$POS1$":
                                    cell.setCellValue(protocol.getPosition1());
                                    break;
                                case "$POS2$":
                                    cell.setCellValue(protocol.getPosition2());
                                    break;
                                case "$POS1NAME$":
                                    cell.setCellValue(String.format("/%s/", protocol.getPosition1FullName()));
                                    break;
                                case "$POS2NAME$":
                                    cell.setCellValue(String.format("/%s/", protocol.getPosition2FullName()));
                                    break;
                                case "$DATE$":
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
                                    cell.setCellValue(sdf.format(protocol.getMillis()));
                                    break;
                                default:
                                    if (cell.getStringCellValue().contains("$")) {
                                        cell.setCellValue("");
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                wb.write(out);
            } finally {
                out.close();
            }
            return out;
        }
    }
}