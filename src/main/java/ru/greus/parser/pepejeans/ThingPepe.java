package ru.greus.parser.pepejeans;

import ru.greus.parser.core.Thing;

import java.util.*;

/**
 * Created by Strel on 03.08.2016.
 */

/** Класс "Вещь" включает в себя основные параметры одежды
 * (артикул, наименование, ссылка на товар, ссылки на картинки,
 *  размеры, цвета, состав, цена).<br/>
 *  Артикул и наименование задаются через конструктор, остальный праметры через сеттер. */
public class ThingPepe extends Thing {
    /** Наименование */
    private String name;
    /** Ссылка на товар */
    private String url;
    /** Ссылки на картинки */
    private List<String> gallery = new ArrayList<>();
    /** Параметры, берем со страницы с товаром */
    private Map<String, String> params = new TreeMap<>();
    /** Цвета(код, название, ссылка на картинку) */
    private List<List<String>> colors = new ArrayList<>();
    /** Цена */
    private float price;

    ThingPepe(String article, String name, String url) {
        super(article);
        this.name = name;
        this.url = url;
    }

    void addImageToGallery(String image) {
        this.gallery.add(image);
    }

    void addParam(String key, String value) {
        this.params.put(key, value);
    }

    void addColor(String colorId, String colorName, String colorURL) {
        this.colors.add(Arrays.asList(colorId, colorName, colorURL));
    }

    void setPrice(float price) {
        this.price = price;
    }

    String getName() {
        return name;
    }

    String getUrl() {
        return url;
    }

    List<String> getGallery() {
        return gallery;
    }

    Map<String, String> getParams() {
        return params;
    }

    List<List<String>> getColors() {
        return colors;
    }

    float getPrice() {
        return price;
    }

    @Override
    public String toSP() {
        String result = "";
        for (String image : this.getGallery()) {
            result += "[img]" + image + "[/img]";
        }
        result += "\n[b]" + this.getArticle() + " " + this.getName() + " - " + this.getPrice() + "[/b]\n";
        for (Map.Entry<String, String> param : this.getParams().entrySet()) {
            result += param.getKey() + " " + param.getValue() + "\n";
        }
        for (List<String> color : this.getColors()) {
            result += color.get(0) + " " + color.get(1) + " [img width=40 height=40]" + color.get(2) + "[/img]\n\n";
        }
        return result;
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
