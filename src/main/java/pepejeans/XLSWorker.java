package pepejeans;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Strel on 14.12.2016.
 */
public class XLSWorker {

    private final static String DEFAULT_FILENAME = "Бланк_заказа_-_men_women.xls"; // Имя файла по умолчанию
    private final static int DEFAULT_SHEET_NUM = 1; // Номер листа по умолчанию
    private final static int DEFAULT_FIRST_ROW_NUM = 6; // Номер первой строки по умолчанию

    private static Workbook wb; // Рабочая книга
    private static int sheetNum; // Номер листа
    private static int firstRowNum; // Номер первой строки

    /* TODO сделать 2 варианта парсера:
     1 - Ищет по коду из парсера сайта и парсит для каждого такого кода
     2 - Парсит весь XLS с последующим объединением в один список вещей
     Замерить, что выходит быстрее по производительности
      */

    public XLSWorker() { // Конструктор по умолчанию
        new XLSWorker(DEFAULT_FILENAME); // TODO Проверить работоспособность с новым прайсом
    }

    private XLSWorker(String fileName) { // Конструктор с листом и первой строкой по умолчанию, имя файла задаем явно
        new XLSWorker(fileName, DEFAULT_SHEET_NUM, DEFAULT_FIRST_ROW_NUM);
    }

    private XLSWorker(String fileName, int sheetNum, int firstRowNum) { // Все параметры задаем явно
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
            boolean isXLSX = fileName.substring(fileName.indexOf('.') + 1).equals("xlsx");
            wb = isXLSX ? new XSSFWorkbook(in) : new HSSFWorkbook(in);
        } catch (IOException e){
            e.printStackTrace();
        }
        XLSWorker.sheetNum = sheetNum;
        XLSWorker.firstRowNum = firstRowNum;
    }

    public String parse(List<ThingPepe> dressList){
        Iterator<ThingPepe> it = dressList.iterator();
        Sheet sheet = wb.getSheetAt(sheetNum);
        while (it.hasNext()) {
            ThingPepe thing = it.next();
            for (int i = firstRowNum; i < sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                try {
                    if (row.getCell(7).toString().equals(thing.getArticle())) { // 7й столбец с артикулом
                        thing.setPrice((float)row.getCell(28).getNumericCellValue()); // 28й столбец с ценой
                        break;
                    } else {
                        //System.out.println("Not found");
                    }
                } catch (NullPointerException e) {
                }
            }
        }

        cleanUpDressList(dressList); // Удаление вещей с пустой ценой

        return dressList.toString();
    }

    private void cleanUpDressList(List<ThingPepe> dressList) {
        dressList.removeIf(thing -> thing.getPrice() == 0.0);
    }

    public static void main(String args[]) {
        SiteWorker site = new SiteWorker();
        XLSWorker excel = new XLSWorker();
        System.out.println(excel.parse(site.parse()));
    }
}
