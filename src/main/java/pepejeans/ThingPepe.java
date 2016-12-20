package pepejeans;

import core.Thing;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

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
    private String[] gallery;
    /** Параметры, берем со страницы с товаром */
    private Map<String, String> params = new TreeMap<>();
    /** Цвета */
    private String[] colors;
    /** Цена */
    private float price;

    ThingPepe(String article, String name, String url) {
        super(article);
        this.name = name;
        this.url = url;
    }

    void setGallery(String[] gallery) {
        this.gallery = gallery;
    }

    void setParams(String key, String value) {
        this.params.put(key, value);
    }

    void setColors(String[] colors) {
        this.colors = colors;
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

    String[] getGallery() {
        return gallery;
    }

    Map<String, String> getParams() {
        return params;
    }

    String[] getColors() {
        return colors;
    }

    float getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Атикул: " + this.getArticle() + "\n" +
                "Наименование: " + name + "\n" +
                "Ссылка на товар: " + url + "\n" +
                "Ссылки на картинки: " + Arrays.toString(gallery) + "\n" +
                "Размеры: " + params + "\n" +
                "Цвета: " + Arrays.toString(colors) + "\n" +
                "Цена: " + price + "\n";
    }
}
