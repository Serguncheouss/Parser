package ru.greus.parser.pepejeans;

import java.io.*;
import java.util.*;

import com.gistlabs.mechanize.document.html.HtmlDocument;
import com.gistlabs.mechanize.document.html.HtmlElement;
import com.gistlabs.mechanize.document.node.Node;
import com.gistlabs.mechanize.exceptions.MechanizeException;
import com.gistlabs.mechanize.impl.MechanizeAgent;
import ru.greus.parser.core.Thing;

/**
 * Created by Strel on 06.08.2016.
 */
public class SiteWorker {
    /** Создание объекта парсера MechanizeAgent */
    private MechanizeAgent agent = new MechanizeAgent();

    private static int testCounter = 0; // Счетчик тестовых итераций
    private static final int TEST_END_COUNT = 100; // Количество тестовых итераций
    private static boolean isTest = true;

    public SiteWorker() {
        /* Название файла с данными авторизации */
        String accountDataFile = "accountPepe.txt";
        try (FileInputStream accountDataStream = new FileInputStream(System.getProperty("user.dir") + "\\" + accountDataFile)) { // открываем файл с данными аккаунта
            int i;
            String buffer = "";
            while ((i = accountDataStream.read()) != -1) { // читаем данные из файла
                buffer += (char) i;
            }
            /* Логин */
            String login = buffer.substring(0, buffer.indexOf("\n") - 1);
            /* Пароль */
            String password = buffer.substring(buffer.indexOf("\n") + 1, buffer.length());
            /* Ссылка на страницу авторизацию */
            String accountUrl = "http://webtool.pepejeans.com/pjlweb/ajax_actions.php?action=login&login_user=" + login + "&login_password=" + password;
            login(accountUrl);
        }
        catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
    public static void main(String[] args) throws IOException {
        XLSWorker excel = null;
        try {
            excel = new XLSWorker("Orderform AW17PC - клиент.xlsx", 1, 7);
        } catch (FileNotFoundException e) {
            System.exit(1);
        }
        SiteWorker siteWorker = new SiteWorker();

        siteWorker.parse(excel.parse());
        //siteWorker.getThings();
        //siteWorker.findThing("PM200143M84"); // для теста
    }
    private void login(String accountUrl) {
        try {
            agent.get(accountUrl);
        }
        catch (MechanizeException e) {
            System.out.println("Ошибка: Сайт недоступен.");
        }
    }
    private HtmlDocument getPage(String url) {
        HtmlDocument page = null;
        try {
            page = agent.get(url);
        }
        catch (MechanizeException e) {
            System.out.println("Ошибка: Сайт недоступен.");
            while (page == null) {
                System.out.println("Попытка восстановления доступа...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                page = agent.get(url);
            }
        }
        return page;
    }
    public List<ThingPepe> parse(List<ThingPepe> dressList) {
        Long startTime = isTest ? System.currentTimeMillis() : null;
        Iterator<ThingPepe> it = dressList.iterator();
        for (int i = 0; isTest && (i < testCounter); i++) {
            it.next();
        }
        while (it.hasNext()) {
            ThingPepe thing = it.next();
            getParamsForThing(thing);
            if (thing.getGallery().size() < 1 || thing.getColors().size() < 1) {
                System.out.println("Товар " + thing.getArticle() + " удален, нет картинки или цвета.");
                it.remove();
            } else {
                System.out.println("Товар " + thing.getArticle() + " добавлен " + Thing.counter + " из " + dressList.size());
                Thing.counter++;
            }
            if (isTest) {
                if (testCounter <= TEST_END_COUNT) testCounter++;
                else break;
            }
        }
        if (isTest) {
            Long endTime = System.currentTimeMillis();
            System.out.println("Время на парсинг " + TEST_END_COUNT + " вещей составило: " +
                    ((endTime - startTime) / 1000) + " секунд.");
            System.exit(1);
        }
        return dressList;
    }
    /** Парсит параметры для товара */
    private void getParamsForThing (ThingPepe thing) {
        String attrName;
        HtmlDocument page = getPage(thing.getUrl());
        // <-- Парсим картинки
        Iterator<? extends Node> it = page.findAll("div.images").iterator();
        String tUrl;
        /* Ссылка на сайт */
        String siteUrl = "http://webtool.pepejeans.com";
        while (it.hasNext()) {
            Node image = it.next();
            if (image.find("div.cancelled_colour") == null && image.find("span.cancelled") == null &&
                    !image.find("img").getAttribute("src").contains("not_available")) { // если цвет не отменен
                tUrl = image.find("a").getAttribute("href");
                thing.addImageToGallery(siteUrl + tUrl.substring(tUrl.indexOf("imagen=") + 7));
            }
        }
        if (thing.getGallery().size() < 1) return;
        // --> Парсим картинки
        // <-- Парсим цвета
        it = page.findAll("div.color").iterator();
        if (!it.hasNext()) {
            HtmlElement washCode = page.find("div#wash_code");
            String colorId = washCode.find("div.name").getValue();
            String colorUrl = siteUrl + washCode.find("img").getAttribute("src");
            thing.addColor(colorId, "", colorUrl);
        }
        while (it.hasNext()) {
            Node node = it.next();
            if (node.find("div.cancelled_icon") == null) {
                String colorId = node.find("div.name").getValue();
                String colorName = node.find("div.small").getAttribute("title");
                String colorUrl = siteUrl + node.find("img").getAttribute("src");
                thing.addColor(colorId, colorName, colorUrl);
            }
        }
        if (thing.getColors().size() < 1) return;
        // --> Парсим цвета
        // <-- Парсим параметры
        it = page.find("ul.info").findAll("li").iterator();
        while (it.hasNext()) {
            Node li = it.next();
            if ((attrName = li.find("span").getValue()).equals("Block:")) {
                break;
            }
            thing.addParam(attrName, li.find("strong").getValue());
        }
        // --> Парсим параметры
    }
}

