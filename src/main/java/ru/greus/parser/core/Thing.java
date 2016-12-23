package ru.greus.parser.core;

/**
 * Created by Strel on 03.08.2016.
 */

/** Абстрактный класс "Вещь" включает в себя только артикул, который задается через конструктор */
public abstract class Thing {
    /** Атикул */
    private String article;

    /** Конструктор по умолчанию отсутствует */
    protected Thing(String article) {
        this.article = article;
    }

    /** Возвращает артикул */
    public String getArticle() {
        return article;
    }
    /** Обязательное переопределение метода toSP() - приводит массив вещей в формат СП и сохраняет в result.txt */
    public abstract String toSP();

    /** Обязательное переопределение метода toString() */
     @Override
    public abstract String toString();
}