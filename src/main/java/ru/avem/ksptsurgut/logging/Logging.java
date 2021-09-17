package ru.avem.ksptsurgut.logging;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.avem.ksptsurgut.Main;
import ru.avem.ksptsurgut.db.model.Protocol;
import ru.avem.ksptsurgut.utils.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static ru.avem.ksptsurgut.utils.Utils.copyFileFromStream;

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

            out = convertProtocolToWorkbookPhase3(protocol);
            FileOutputStream fileOut = new FileOutputStream(file);
            out.writeTo(fileOut);
            out.close();
            fileOut.close();
        } catch (IOException | InvalidFormatException e) {
            return false;
        }
        return true;
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
                                    cell.setCellValue(protocol.getE1UBH());
                                    break;
                                case "$202$":
                                    cell.setCellValue(protocol.getE1R15BH());
                                    break;
                                case "$203$":
                                    cell.setCellValue(protocol.getE1R60BH());
                                    break;
                                case "$204$":
                                    cell.setCellValue(protocol.getE1CoefBH());
                                    break;
                                case "$205$":
                                    cell.setCellValue(protocol.getE1ResultBH());
                                    break;
                                case "$206$":
                                    cell.setCellValue(protocol.getE1UHH());
                                    break;
                                case "$207$":
                                    cell.setCellValue(protocol.getE1R15HH());
                                    break;
                                case "$208$":
                                    cell.setCellValue(protocol.getE1R60HH());
                                    break;
                                case "$209$":
                                    cell.setCellValue(protocol.getE1CoefHH());
                                    break;
                                case "$210$":
                                    cell.setCellValue(protocol.getE1ResultHH());
                                    break;
                                case "$211$":
                                    cell.setCellValue(protocol.getE1UBHHH());
                                    break;
                                case "$212$":
                                    cell.setCellValue(protocol.getE1R15BHHH());
                                    break;
                                case "$213$":
                                    cell.setCellValue(protocol.getE1R60BHHH());
                                    break;
                                case "$214$":
                                    cell.setCellValue(protocol.getE1CoefBHHH());
                                    break;
                                case "$215$":
                                    cell.setCellValue(protocol.getE1ResultBHHH());
                                    break;
                                case "$1$":
                                    cell.setCellValue(protocol.getE2WindingBH());
                                    break;
                                case "$2$":
                                    cell.setCellValue(protocol.getE2ABBH());
                                    break;
                                case "$3$":
                                    cell.setCellValue(protocol.getE2BCBH());
                                    break;
                                case "$4$":
                                    cell.setCellValue(protocol.getE2CABH());
                                    break;
                                case "$5$":
                                    cell.setCellValue(protocol.getE2TBH());
                                    break;
                                case "$6$":
                                    cell.setCellValue(protocol.getE2ResultBH());
                                    break;
                                case "$422$":
                                    cell.setCellValue(protocol.getE2ABBH2());
                                    break;
                                case "$423$":
                                    cell.setCellValue(protocol.getE2BCBH2());
                                    break;
                                case "$424$":
                                    cell.setCellValue(protocol.getE2CABH2());
                                    break;
                                case "$425$":
                                    cell.setCellValue(protocol.getE2TBH2());
                                    break;
                                case "$426$":
                                    cell.setCellValue(protocol.getE2ResultBH2());
                                    break;
                                case "$432$":
                                    cell.setCellValue(protocol.getE2ABBH3());
                                    break;
                                case "$433$":
                                    cell.setCellValue(protocol.getE2BCBH3());
                                    break;
                                case "$434$":
                                    cell.setCellValue(protocol.getE2CABH3());
                                    break;
                                case "$435$":
                                    cell.setCellValue(protocol.getE2TBH3());
                                    break;
                                case "$436$":
                                    cell.setCellValue(protocol.getE2ResultBH3());
                                    break;
                                case "$442$":
                                    cell.setCellValue(protocol.getE2ABBH4());
                                    break;
                                case "$443$":
                                    cell.setCellValue(protocol.getE2BCBH4());
                                    break;
                                case "$444$":
                                    cell.setCellValue(protocol.getE2CABH4());
                                    break;
                                case "$445$":
                                    cell.setCellValue(protocol.getE2TBH4());
                                    break;
                                case "$446$":
                                    cell.setCellValue(protocol.getE2ResultBH4());
                                    break;
                                case "$452$":
                                    cell.setCellValue(protocol.getE2ABBH5());
                                    break;
                                case "$453$":
                                    cell.setCellValue(protocol.getE2BCBH5());
                                    break;
                                case "$454$":
                                    cell.setCellValue(protocol.getE2CABH5());
                                    break;
                                case "$455$":
                                    cell.setCellValue(protocol.getE2TBH5());
                                    break;
                                case "$456$":
                                    cell.setCellValue(protocol.getE2ResultBH5());
                                    break;
                                case "$7$":
                                    cell.setCellValue(protocol.getE2WindingHH());
                                    break;
                                case "$8$":
                                    cell.setCellValue(protocol.getE2ABHH());
                                    break;
                                case "$9$":
                                    cell.setCellValue(protocol.getE2BCHH());
                                    break;
                                case "$10$":
                                    cell.setCellValue(protocol.getE2CAHH());
                                    break;
                                case "$11$":
                                    cell.setCellValue(protocol.getE2THH());
                                    break;
                                case "$12$":
                                    cell.setCellValue(protocol.getE2ResultHH());
                                    break;
                                case "$13$":
                                    cell.setCellValue(protocol.getE3UInputAB());
                                    break;
                                case "$14$":
                                    cell.setCellValue(protocol.getE3UInputBC());
                                    break;
                                case "$15$":
                                    cell.setCellValue(protocol.getE3UInputCA());
                                    break;
                                case "$16$":
                                    cell.setCellValue(protocol.getE3UInputAvr());
                                    break;
                                case "$17$":
                                    cell.setCellValue(protocol.getE3UOutputAB());
                                    break;
                                case "$18$":
                                    cell.setCellValue(protocol.getE3UOutputBC());
                                    break;
                                case "$19$":
                                    cell.setCellValue(protocol.getE3UOutputCA());
                                    break;
                                case "$20$":
                                    cell.setCellValue(protocol.getE3UOutputAvr());
                                    break;
                                case "$21$":
                                    cell.setCellValue(protocol.getE3DiffU());
                                    break;
                                case "$22$":
                                    cell.setCellValue(protocol.getE3F());
                                    break;
                                case "$23$":
                                    cell.setCellValue(protocol.getE3Result());
                                    break;
                                case "$613$":
                                    cell.setCellValue(protocol.getE3UInputAB2());
                                    break;
                                case "$614$":
                                    cell.setCellValue(protocol.getE3UInputBC2());
                                    break;
                                case "$615$":
                                    cell.setCellValue(protocol.getE3UInputCA2());
                                    break;
                                case "$616$":
                                    cell.setCellValue(protocol.getE3UInputAvr2());
                                    break;
                                case "$617$":
                                    cell.setCellValue(protocol.getE3UOutputAB2());
                                    break;
                                case "$618$":
                                    cell.setCellValue(protocol.getE3UOutputBC2());
                                    break;
                                case "$619$":
                                    cell.setCellValue(protocol.getE3UOutputCA2());
                                    break;
                                case "$620$":
                                    cell.setCellValue(protocol.getE3UOutputAvr2());
                                    break;
                                case "$621$":
                                    cell.setCellValue(protocol.getE3DiffU2());
                                    break;
                                case "$622$":
                                    cell.setCellValue(protocol.getE3F2());
                                    break;
                                case "$623$":
                                    cell.setCellValue(protocol.getE3Result2());
                                    break;


                                case "$713$":
                                    cell.setCellValue(protocol.getE3UInputAB3());
                                    break;
                                case "$714$":
                                    cell.setCellValue(protocol.getE3UInputBC3());
                                    break;
                                case "$715$":
                                    cell.setCellValue(protocol.getE3UInputCA3());
                                    break;
                                case "$716$":
                                    cell.setCellValue(protocol.getE3UInputAvr3());
                                    break;
                                case "$717$":
                                    cell.setCellValue(protocol.getE3UOutputAB3());
                                    break;
                                case "$718$":
                                    cell.setCellValue(protocol.getE3UOutputBC3());
                                    break;
                                case "$719$":
                                    cell.setCellValue(protocol.getE3UOutputCA3());
                                    break;
                                case "$720$":
                                    cell.setCellValue(protocol.getE3UOutputAvr3());
                                    break;
                                case "$721$":
                                    cell.setCellValue(protocol.getE3DiffU3());
                                    break;
                                case "$722$":
                                    cell.setCellValue(protocol.getE3F3());
                                    break;
                                case "$723$":
                                    cell.setCellValue(protocol.getE3Result3());
                                    break;


                                case "$813$":
                                    cell.setCellValue(protocol.getE3UInputAB4());
                                    break;
                                case "$814$":
                                    cell.setCellValue(protocol.getE3UInputBC4());
                                    break;
                                case "$815$":
                                    cell.setCellValue(protocol.getE3UInputCA4());
                                    break;
                                case "$816$":
                                    cell.setCellValue(protocol.getE3UInputAvr4());
                                    break;
                                case "$817$":
                                    cell.setCellValue(protocol.getE3UOutputAB4());
                                    break;
                                case "$818$":
                                    cell.setCellValue(protocol.getE3UOutputBC4());
                                    break;
                                case "$819$":
                                    cell.setCellValue(protocol.getE3UOutputCA4());
                                    break;
                                case "$820$":
                                    cell.setCellValue(protocol.getE3UOutputAvr4());
                                    break;
                                case "$821$":
                                    cell.setCellValue(protocol.getE3DiffU4());
                                    break;
                                case "$822$":
                                    cell.setCellValue(protocol.getE3F4());
                                    break;
                                case "$823$":
                                    cell.setCellValue(protocol.getE3Result4());
                                    break;


                                case "$913$":
                                    cell.setCellValue(protocol.getE3UInputAB5());
                                    break;
                                case "$914$":
                                    cell.setCellValue(protocol.getE3UInputBC5());
                                    break;
                                case "$915$":
                                    cell.setCellValue(protocol.getE3UInputCA5());
                                    break;
                                case "$916$":
                                    cell.setCellValue(protocol.getE3UInputAvr5());
                                    break;
                                case "$917$":
                                    cell.setCellValue(protocol.getE3UOutputAB5());
                                    break;
                                case "$918$":
                                    cell.setCellValue(protocol.getE3UOutputBC5());
                                    break;
                                case "$919$":
                                    cell.setCellValue(protocol.getE3UOutputCA5());
                                    break;
                                case "$920$":
                                    cell.setCellValue(protocol.getE3UOutputAvr5());
                                    break;
                                case "$921$":
                                    cell.setCellValue(protocol.getE3DiffU5());
                                    break;
                                case "$922$":
                                    cell.setCellValue(protocol.getE3F5());
                                    break;
                                case "$923$":
                                    cell.setCellValue(protocol.getE3Result5());
                                    break;


                                case "$28$":
                                    cell.setCellValue(protocol.getE3WindingBH());
                                    break;
                                case "$29$":
                                    cell.setCellValue(protocol.getE3WindingHH());
                                    break;
                                case "$27$":
                                    cell.setCellValue(protocol.getE3Result());
                                    break;
                                case "$33$":
                                    cell.setCellValue(protocol.getE4UKZV());
                                    break;
                                case "$34$":
                                    cell.setCellValue(protocol.getE4UKZPercent());
                                    break;
                                case "$134$":
                                    cell.setCellValue(protocol.getE4UKZDiff());
                                    break;
                                case "$35$":
                                    cell.setCellValue(protocol.getE4IA());
                                    break;
                                case "$36$":
                                    cell.setCellValue(protocol.getE4IB());
                                    break;
                                case "$37$":
                                    cell.setCellValue(protocol.getE4IC());
                                    break;
                                case "$38$":
                                    cell.setCellValue(protocol.getE4Pp());
                                    break;
                                case "$39$":
                                    cell.setCellValue(protocol.getE4F());
                                    break;
                                case "$40$":
                                    cell.setCellValue(protocol.getE4Result());
                                    break;
                                case "$41$":
                                    cell.setCellValue(protocol.getE5UBH());
                                    break;
                                case "$42$":
                                    cell.setCellValue(protocol.getE5IA());
                                    break;
                                case "$43$":
                                    cell.setCellValue(protocol.getE5IB());
                                    break;
                                case "$44$":
                                    cell.setCellValue(protocol.getE5IC());
                                    break;
                                case "$142$":
                                    cell.setCellValue(protocol.getE5IADiff());
                                    break;
                                case "$143$":
                                    cell.setCellValue(protocol.getE5IBDiff());
                                    break;
                                case "$144$":
                                    cell.setCellValue(protocol.getE5ICDiff());
                                    break;
                                case "$45$":
                                    cell.setCellValue(protocol.getE5Pp());
                                    break;
                                case "$46$":
                                    cell.setCellValue(protocol.getE5Cos());
                                    break;
                                case "$47$":
                                    cell.setCellValue(protocol.getE5F());
                                    break;
                                case "$48$":
                                    cell.setCellValue(protocol.getE5Result());
                                    break;
                                case "$49$":
                                    cell.setCellValue(protocol.getE6UInput());
                                    break;
                                case "$50$":
                                    cell.setCellValue(protocol.getE6IBH());
                                    break;
                                case "$51$":
                                    cell.setCellValue(protocol.getE6F());
                                    break;
                                case "$52$":
                                    cell.setCellValue(protocol.getE6Time());
                                    break;
                                case "$53$":
                                    cell.setCellValue(protocol.getE6Result());
                                    break;
                                case "$54$":
                                    cell.setCellValue(protocol.getE7TypeBHandCorps());
                                    break;
                                case "$55$":
                                    cell.setCellValue(protocol.getE7UBHAvem());
                                    break;
                                case "$56$":
                                    cell.setCellValue(protocol.getE7IBHandCorps());
                                    break;
                                case "$57$":
                                    cell.setCellValue(protocol.getE7TimeBHandCorps());
                                    break;
                                case "$58$":
                                    cell.setCellValue(protocol.getE7ResultBHandCorps());
                                    break;
                                case "$59$":
                                    cell.setCellValue(protocol.getE7TypeHHandCorps());
                                    break;
                                case "$60$":
                                    cell.setCellValue(protocol.getE7UHHAvem());
                                    break;
                                case "$61$":
                                    cell.setCellValue(protocol.getE7IHHandCorps());
                                    break;
                                case "$62$":
                                    cell.setCellValue(protocol.getE7TimeHHandCorps());
                                    break;
                                case "$63$":
                                    cell.setCellValue(protocol.getE7ResultHHandCorps());
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
                                case "$110$":
                                    cell.setCellValue(protocol.getUmeger());
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