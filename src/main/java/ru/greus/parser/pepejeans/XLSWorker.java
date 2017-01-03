package ru.greus.parser.pepejeans;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by Strel on 14.12.2016.
 */
public class XLSWorker {

    private final static String DEFAULT_FILENAME = "Бланк_заказа_-_men_women.xls"; // Имя файла по умолчанию
    private final static int DEFAULT_SHEET_NUM = 1; // Номер листа по умолчанию
    private final static int DEFAULT_FIRST_ROW_NUM = 6; // Номер первой строки по умолчанию
    private final static int DEFAULT_ARTICLE_COL = 4; // Столбец с артикулом по умолчанию
    private final static int DEFAULT_NAME_COL = 6; // Столбец с наименованием по умолчанию
    private final static int DEFAULT_LINEBOOK_COL = 13; // Столбец с артикулом по умолчанию**
    private final static int DEFAULT_DIVISION_COL = 0; // Столбец с наименованием по умолчанию**
    private final static int DEFAULT_THEME_COL = 3; // Столбец с наименованием по умолчанию**
    private final static int DEFAULT_PRICE_COL = 29/*28*/; // Столбец с ценой по умолчанию

    private static Workbook wb; // Рабочая книга
    private static int sheetNum; // Номер листа
    private static int firstRowNum; // Номер первой строки

    /** Конструктор по умолчанию */
    public XLSWorker() throws FileNotFoundException {
        new XLSWorker(DEFAULT_FILENAME);
    }
    /** Конструктор с листом и первой строкой по умолчанию, имя файла задаем явно */
    public XLSWorker(String fileName) throws FileNotFoundException {
        new XLSWorker(fileName, DEFAULT_SHEET_NUM, DEFAULT_FIRST_ROW_NUM);
    }
    /** Конструктор с именем файла по умолчанию, лист и первую строку задаем явно */
    public XLSWorker(int sheetNum, int firstRowNum) throws FileNotFoundException {
        new XLSWorker(DEFAULT_FILENAME, sheetNum, firstRowNum);
    }
    /** Конструктор все параметры задаем явно */
    public XLSWorker(String fileName, int sheetNum, int firstRowNum) throws FileNotFoundException {
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
    /** Парсит эксель файл */
    public List<ThingPepe> parse(){
        List<ThingPepe> dressList = new ArrayList<>();
        Sheet sheet = wb.getSheetAt(sheetNum);
        ThingPepe thing;
        for (int i = firstRowNum; i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Cell article = row.getCell(DEFAULT_ARTICLE_COL);
            Cell name = row.getCell(DEFAULT_NAME_COL);
            Cell linebook = row.getCell(DEFAULT_LINEBOOK_COL);
            Cell division = row.getCell(DEFAULT_DIVISION_COL);
            Cell theme = row.getCell(DEFAULT_THEME_COL);
            Cell price = row.getCell(DEFAULT_PRICE_COL);
            if ((article != null && price != null && theme.toString().equals("")) && // Если цена и тема не нулевые
               !(dressList.size() > 0 && // Обязательно для строки ниже
               article.toString().equals(dressList.get(dressList.size() - 1).getArticle()))) { // Убирает дубли
                    dressList.add(thing = new ThingPepe(article.toString()));
                    thing.setName(name.toString());
                    thing.setSeason(linebook.toString().substring(0, 2));
                    thing.setYear(linebook.toString().substring(2, 4));
                    thing.setRollout(linebook.toString().substring(4, 6));
                    thing.setDivision(division.toString());
                    thing.setTheme(theme.toString().replace(' ', '+'));
                    thing.setPrice((float) price.getNumericCellValue());
                    thing.setUrl();
            }
        }

        System.out.println("Добавлено " + dressList.size() + " вещей из прайса.");
        return dressList;
    }

    public static void main(String args[]) {
        XLSWorker excel = null;
        try {
            excel = new XLSWorker("Orderform AW17PC - клиент.xlsx", 1, 7);
        } catch (FileNotFoundException e) {
            System.exit(1);
        }
        System.out.println(excel.parse());
    }
}
