package ru.greus.parser.pepejeans;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Strel on 14.12.2016.
 */
public class XLSWorker {

    private final static String DEFAULT_FILENAME = "Бланк_заказа_-_men_women.xls"; // Имя файла по умолчанию
    private final static int DEFAULT_SHEET_NUM = 1; // Номер листа по умолчанию
    private final static int DEFAULT_FIRST_ROW_NUM = 6; // Номер первой строки по умолчанию
    private final static int DEFAULT_ARTICLE_COL = 4/*7*/; // Столбец с артикулом по умолчанию
    private final static int DEFAULT_PRICE_COL = 29/*28*/; // Столбец с ценой по умолчанию

    private static Workbook wb; // Рабочая книга
    private static int sheetNum; // Номер листа
    private static int firstRowNum; // Номер первой строки

    /* TODO сделать 2 варианта парсера:
     1 - Ищет по коду из парсера сайта и парсит для каждого такого кода
     2 - Парсит весь XLS с последующим объединением в один список вещей
     Замерить, что выходит быстрее по производительности
      */

    public XLSWorker() { // Конструктор по умолчанию
        new XLSWorker(DEFAULT_FILENAME);
    }

    public XLSWorker(String fileName) { // Конструктор с листом и первой строкой по умолчанию, имя файла задаем явно
        new XLSWorker(fileName, DEFAULT_SHEET_NUM, DEFAULT_FIRST_ROW_NUM);
    }

    public XLSWorker(int sheetNum, int firstRowNum) { // Конструктор с именем файла по умолчанию, лист и первую строку задаем явно
        new XLSWorker(DEFAULT_FILENAME, sheetNum, firstRowNum);
    }

    public XLSWorker(String fileName, int sheetNum, int firstRowNum) { // Все параметры задаем явно
        try {
            FileInputStream fin = new FileInputStream(System.getProperty("user.dir") + "\\" + fileName);
            boolean isXLSX = fileName.substring(fileName.indexOf('.') + 1).equals("xlsx");
            wb = isXLSX ? new XSSFWorkbook(fin) : new HSSFWorkbook(fin);
        } catch (IOException e){
            e.printStackTrace();
        }
        XLSWorker.sheetNum = sheetNum;
        XLSWorker.firstRowNum = firstRowNum;
    }

    public List<ThingPepe> parse(List<ThingPepe> dressList){
        Iterator<ThingPepe> it = dressList.iterator();
        Sheet sheet = wb.getSheetAt(sheetNum);
        while (it.hasNext()) {
            ThingPepe thing = it.next();
            for (int i = firstRowNum; i < sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                try {
                    if (row.getCell(DEFAULT_ARTICLE_COL).toString().equals(thing.getArticle())) {
                        thing.setPrice((float)row.getCell(DEFAULT_PRICE_COL).getNumericCellValue());
                        break;
                    } else {
                        //System.out.println("Not found");
                    }
                } catch (NullPointerException e) {
                }
            }
        }

        cleanUpDressList(dressList); // Удаление вещей с пустой ценой

        return dressList;
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
