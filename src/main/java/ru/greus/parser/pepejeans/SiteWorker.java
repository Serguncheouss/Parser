package ru.greus.parser.pepejeans;

import java.io.*;
import java.util.*;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by Strel on 06.08.2016.
 */
public class SiteWorker implements Callable<SiteWorker> {
    /** Название файла с данными авторизации */
    private static final String ACCOUNT_DATA_FILE = "accountPepe.txt";

    private URL accountUrl;
    private Map<String, String> cookies;
    private ThingPepe thing;
    private int thingNumber;
    private int thingTotal;

    public SiteWorker(ThingPepe thing, int thingNumber, int thingTotal) {
        cookies = login(getAccountUrl());
        this.thing = thing;
        this.thingNumber = thingNumber;
        this.thingTotal = thingTotal;
    }

    /** Получение данных авторизации */
    private URL getAccountUrl() {
        try (FileInputStream accountDataStream = new FileInputStream(System.getProperty("user.dir") + "\\" + ACCOUNT_DATA_FILE)) { // открываем файл с данными аккаунта
            int i;
            String buffer = "";
            while ((i = accountDataStream.read()) != -1) { // читаем данные из файла
                buffer += (char) i;
            }
            /* Логин */
            String login = buffer.substring(0, buffer.indexOf("\n") - 1);
            /* Пароль */
            String password = buffer.substring(buffer.indexOf("\n") + 1, buffer.length());
            accountUrl = new URL("http://webtool.pepejeans.com/pjlweb/ajax_actions.php?action=login&login_user=" +
                    login + "&login_password=" + password);
        }
        catch (IOException e) {
            System.out.println("[Error] - Ошибка: " + e.getMessage());
            System.exit(1);
        }
        return accountUrl;
    }
    /** Авторизация а сайте */
    private Map<String, String> login(URL accountUrl) {
        Document accountPage = null;
        Connection.Response res = null;
        try {
            res = Jsoup.connect(accountUrl.toString()).timeout(0).execute();
            accountPage = res.parse();
        }
        catch (Exception e) {
            System.out.println("[Error] - Ошибка: " + e.getMessage());
            while (accountPage == null) {
                System.out.println("[Try] - Попытка восстановления доступа...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                try {
                    res = Jsoup.connect(accountUrl.toString()).timeout(0).execute();
                    accountPage = res.parse();
                } catch (Exception e1) {
                    System.out.println("[Error] - Ошибка: " + e1.getMessage());
                }
            }
        }
        if (accountPage.body().ownText().equals("true")) {
            return res.cookies();
        }
        else {
            System.out.println("[Error] - Авторизация не удалась, возможно неверный логин или пароль.");
            System.exit(1);
            return null;
        }
    }
    /** Загружает страницу с товаром */
    private Document getPage() {
        Document page = null;
        URL thingUrl = thing.getUrl();
        try {
            page = Jsoup.connect(thingUrl.toString()).timeout(0).cookies(cookies).get();
        }
        catch (IOException e) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
            }
            while (page == null) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
                try {
                    page = Jsoup.connect(thingUrl.toString()).timeout(0).cookies(cookies).get();
                } catch (IOException e1) {
                }
            }
        }
        return page;
    }
    /** Парсит параметры для товара */
    private void getParamsForThing(Document page) throws MalformedURLException {
        String attrName;
        // <-- Парсим картинки
        Iterator<Element> it = page.select("div.images").iterator();
        String tUrl;
        /* Ссылка на сайт */
        String siteUrl = "http://webtool.pepejeans.com";
        while (it.hasNext()) {
            Element image = it.next();
            if (image.select("div.cancelled_colour").first() == null &&
                    image.select("span.cancelled").first() == null &&
                    image.select("img[src*=not_available]").first() == null) { // если цвет не отменен
                tUrl = image.select("a").attr("href");
                thing.addImageToGallery(new URL(siteUrl + tUrl.substring(tUrl.indexOf("imagen=") + 7)));
            }
        }
        if (thing.getGallery().size() < 1) return;
        // --> Парсим картинки
        // <-- Парсим цвета
        it = page.select("div.color").iterator();
        if (!it.hasNext()) {
            Elements washCode = page.select("div#wash_code");
            String colorId = washCode.select("div.name").text();
            URL colorUrl = new URL(siteUrl + washCode.select("img").attr("src"));
            thing.addColor(colorId, "", colorUrl);
        }
        while (it.hasNext()) {
            Element node = it.next();
            if (node.select("div.cancelled_icon").first() == null) {
                String colorId = node.select("div.name").text();
                String colorName = node.select("div.small").attr("title");
                URL colorUrl = new URL(siteUrl + node.select("img").attr("src"));
                thing.addColor(colorId, colorName, colorUrl);
            }
        }
        if (thing.getColors().size() < 1) return;
        // --> Парсим цвета
        // <-- Парсим параметры
        it = page.select("ul.info").select("li").iterator();
        while (it.hasNext()) {
            Element li = it.next();
            if ((attrName = li.select("span").text()).equals("Block:")) {
                break;
            }
            thing.addParam(attrName, li.select("strong").text());
        }
        // --> Парсим параметры
    }

    /** Основное тело потока */
    public SiteWorker call() throws Exception {
        Document page = getPage();
        getParamsForThing(page);
        System.out.println("[Ok] - Пропарсили товар " + thing.getArticle() + " - " + thingNumber + " из " + thingTotal + " шт.");
        return this;
    }
    /** Возвращает товар */
    public ThingPepe getThing() {
        return thing;
    }
}

