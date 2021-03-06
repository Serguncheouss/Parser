package ru.greus.parser.pepejeans;

import ru.greus.parser.core.Thing;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Strel on 03.08.2016.
 */

/** Класс "Вещь" включает в себя основные параметры одежды
 * (артикул, наименование, ссылка на товар, ссылки на картинки,
 *  размеры, цвета, состав, цена).<br/>
 *  Артикул и наименование задаются через конструктор, остальный праметры через сеттер. */
public class ThingPepe extends Thing {
    private String season;
    private String year;
    private String rollout;
    private static final String BRAND = "P";
    private String division;
    private static final HashMap<String, String> DIVISION_MAP = new HashMap<String, String>() {{
        put("MENS", "01");
        put("LADIES", "02");
        put("BOYS", "03"); // В прайсе только BOYS, на сайте JUNIOR BOYS
        put("GIRLS", "04"); // В прайсе только GIRLS, на сайте JUNIOR GIRLS
        put("KID BOYS", "06");
        put("KID GIRLS", "07");
        put("UNISEX", "08");
    }};
    private String theme;
    /** Наименование */
    private String name;
    /** Ссылка на товар */
    private String url;
    /** Ссылки на картинки */
    private List<String> gallery = new ArrayList<>();
    /** Параметры, берем со страницы с товаром */
    private Map<String, String> params = new TreeMap<>();
    /** Цвета(код, название, ссылка на картинку) */
    private List<List<String>> colors = new ArrayList<>(3);
    /** Цена */
    private float price;

    ThingPepe(String article) {
        super(article);
    }

    ThingPepe(String article, String name, String url) {
        this(article);
        this.name = name;
        this.url = url;
    }

    String getSeason() {
        return season;
    }

    void setSeason(String season) {
        this.season = season;
    }

    String getYear() {
        return year;
    }

    void setYear(String year) {
        this.year = year;
    }

    String getRollout() {
        return rollout;
    }

    void setRollout(String rollout) {
        this.rollout = rollout;
    }

    static String getBrand() { return BRAND; }

    String getDivision() {
        return division;
    }

    void setDivision(String division) {
        this.division = division;
    }

    static String getDivisionMap(String key) {
        return DIVISION_MAP.get(key);
    }

    String getTheme() {
        return theme;
    }

    void setTheme(String theme) {
        this.theme = theme;
    }

    String getName() {
        return name;
    }

    void setName(String name) { this.name = name; }

    List<String> getGallery() {
        return gallery;
    }

    void addImageToGallery(String image) {
        this.gallery.add(image);
    }

    Map<String, String> getParams() {
        return params;
    }

    void addParam(String key, String value) {
        this.params.put(key, value);
    }

    List<List<String>> getColors() {
        return colors;
    }

    void addColor(String colorId, String colorName, String colorURL) {
        this.colors.add(Arrays.asList(colorId, colorName, colorURL));
    }

    float getPrice() {
        return price;
    }

    void setPrice(float price) {
        this.price = price;
    }

    String getUrl() {
        return url;
    }

    void setUrl() {
        this.url = "http://webtool.pepejeans.com/pjlweb/index.php?section=linebook_detail&season=" +
                getSeason() + "&year=20" + getYear() + "&rollout=" + getRollout() + "&brand=" + getBrand() +
                "&division=" + getDivisionMap(getDivision()) + "&theme=" + getTheme() + "&stylecode=" +
                getArticle() + "&inspiration=1&backid=row_1_0";
    }

    public void toSP(String currentDate) {
        String result = "";
        for (String image : this.getGallery()) {
            result += "[img width=400]" + image + "[/img]";
        }
        result += "\n[b]" + this.getArticle() + " " + this.getName() + " - " + this.getPrice() + " €\n";
        for (Map.Entry<String, String> param : this.getParams().entrySet()) {
            result += param.getKey() + " " + param.getValue() + "\n";
        }
        for (List<String> color : this.getColors()) {
            result += color.get(0) + " " + color.get(1) + " [img width=40 height=40]" + color.get(2) + "[/img]\n\n";
        }

        try {
            FileWriter fw = new FileWriter(currentDate + "_" + this.getDivision() + "_" + this.getTheme() + ".txt", true);
            fw.write(result);
            fw.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Атикул: " + this.getArticle() + "\n" +
                "Наименование: " + getName() + "\n" +
                "Ссылка на товар: " + getUrl() + "\n" +
                "Ссылки на картинки: " + getGallery() + "\n" +
                "Параметры: " + getParams() + "\n" +
                "Цвета: " + getColors() + "\n" +
                "Цена: " + getPrice() + "\n";
    }


}
