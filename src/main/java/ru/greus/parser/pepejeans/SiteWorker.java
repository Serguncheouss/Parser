package ru.greus.parser.pepejeans;

import java.io.*;
import java.util.*;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.greus.parser.core.Thing;

/**
 * Created by Strel on 06.08.2016.
 */
public class SiteWorker extends Thread {
    /** Название файла с данными авторизации */
    private static final String accountDataFile = "accountPepe.txt";
    /** Логин */
    private static String login;
    /** Пароль */
    private static String password;
    /** Ссылка на страницу авторизацию */
    private static String accountUrl;
    /** Создание объекта парсера MechanizeAgent */

    private Document page;

    private volatile static Map<String, String> cookies;

    private ThingPepe thing;

    private static int testCounter = 0; // Счетчик тестовых итераций(начальное значение)
    private static final int TEST_END_COUNT = 100; // Количество тестовых итераций
    private static final boolean isTest = true;

    public SiteWorker() {
        try (FileInputStream accountDataStream = new FileInputStream(System.getProperty("user.dir") + "\\" + accountDataFile)) { // открываем файл с данными аккаунта
            int i;
            String buffer = "";
            while ((i = accountDataStream.read()) != -1) { // читаем данные из файла
                buffer += (char) i;
            }
            login = buffer.substring(0, buffer.indexOf("\n") - 1);
            password = buffer.substring(buffer.indexOf("\n") + 1, buffer.length());
            accountUrl = "http://webtool.pepejeans.com/pjlweb/ajax_actions.php?action=login&login_user=" + login + "&login_password=" + password;
            login(accountUrl);
        }
        catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private SiteWorker(ThingPepe thing) {
//        System.out.println("[Ok] - Создан новый поток - " + this.getName());
        this.thing = thing;
    }

    public void run() {
        this.getParamsForThing(thing);
//        System.out.println("[Ok] - Поток " + this.getName() + ". " + this.thing.toString());
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
    }
    private void login(String accountUrl) {
        Document accountPage = null;
        Connection.Response res = null;
        try {
            System.out.println("[Try] - Авторизация на сайте...");
            res = Jsoup.connect(accountUrl).timeout(0).execute();
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
                    res = Jsoup.connect(accountUrl).timeout(0).execute();
                    accountPage = res.parse();
                } catch (Exception e1) {
                    System.out.println("[Error] - Ошибка: " + e1.getMessage());
                }
            }
        }
        if (accountPage.body().ownText().equals("true")) {
            System.out.println("[Ok] - Авторизация прошла успешно.");
            cookies = res.cookies();
        }
        else {
            System.out.println("[Error] - Авторизация не удалась, возможно неверный логин или пароль.");
            System.exit(1);
        }
    }
    public List<ThingPepe> parse(List<ThingPepe> dressList) {
        Long startTime = isTest ? System.currentTimeMillis() : null;
        Iterator<ThingPepe> it = dressList.iterator();
        for (int i = 0; isTest && (i < testCounter); i++) {
            it.next();
        }
        SiteWorker swThread1 = null;
        SiteWorker swThread2 = null;
        SiteWorker swThread3 = null;
        SiteWorker swThread4 = null;
        SiteWorker swThread5 = null;
        while (it.hasNext()) {
            if (isTest) {
                if (testCounter <= TEST_END_COUNT) testCounter++;
                else break;
            }
            ThingPepe thing = it.next(); // TODO где то здесь многопоточность
            if (swThread1 == null || !swThread1.isAlive()) {
                swThread1 = new SiteWorker(thing);
                swThread1.start();
            } else if (swThread2 == null || !swThread2.isAlive()) {
                swThread2 = new SiteWorker(thing);
                swThread2.start();
            } else if (swThread3 == null || !swThread3.isAlive()) {
                swThread3 = new SiteWorker(thing);
                swThread3.start();
            } else if (swThread4 == null || !swThread4.isAlive()) {
                swThread4 = new SiteWorker(thing);
                swThread4.start();
            } else if (swThread5 == null || !swThread5.isAlive()) {
                swThread5 = new SiteWorker(thing);
                swThread5.start();
            } else {
                while (swThread1.isAlive() & swThread2.isAlive() & swThread3.isAlive() & swThread4.isAlive() &
                        swThread5.isAlive()) {
//                    System.out.println("Ждем...");
                }
            }
            System.out.println("[Ok] - Товар " + thing.getArticle() + " добавлен " +
                    ThingPepe.counter++ + " из " + dressList.size());
        }
        if (isTest) {
            Long endTime = System.currentTimeMillis();
            System.out.println("Время на парсинг " + TEST_END_COUNT + " вещей составило: " +
                    ((endTime - startTime) / 1000) + " минут.");
        }
        return clearDress(dressList);
    }
    private Document getPage(String url) {
        try {
//            System.out.println("[Try] - Поток " + this.getName() + ". Переход на страницу с товаром...");
            page = Jsoup.connect(url).timeout(0).cookies(cookies).get();
        }
        catch (IOException e) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                System.out.println("[Error] - Поток " + this.getName() + ". " + e1.getMessage());
            }
            System.out.println("[Error] - Поток " + this.getName() + ". Ошибка: Сайт недоступен.");
            System.out.println("[Error] - Поток " + this.getName() + ". " + e.getMessage());
            while (page == null) {
                System.out.println("[Try] - Поток " + this.getName() + ". Попытка восстановления доступа...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    System.out.println("[Error] - Поток " + this.getName() + ". " + e1.getMessage());
                }
                try {
                    page = Jsoup.connect(url).timeout(0).cookies(cookies).get();
                } catch (IOException e1) {
                    System.out.println("[Error] - Поток " + this.getName() + ". " + e1.getMessage());
                }
            }
        }
        return page;
    }
    /** Парсит параметры для товара */
    private void getParamsForThing(ThingPepe thing) { // TODO добавить многопоточность
        String attrName;
        page = this.getPage(thing.getUrl());
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
                thing.addImageToGallery(siteUrl + tUrl.substring(tUrl.indexOf("imagen=") + 7));
            }
        }
        if (thing.getGallery().size() < 1) return;
        // --> Парсим картинки
        // <-- Парсим цвета
        it = page.select("div.color").iterator();
        if (!it.hasNext()) {
            Elements washCode = page.select("div#wash_code");
            String colorId = washCode.select("div.name").text();
            String colorUrl = siteUrl + washCode.select("img").attr("src");
            thing.addColor(colorId, "", colorUrl);
        }
        while (it.hasNext()) {
            Element node = it.next();
            if (node.select("div.cancelled_icon") == null) {
                String colorId = node.select("div.name").text();
                String colorName = node.select("div.small").attr("title");
                String colorUrl = siteUrl + node.select("img").attr("src");
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
    private List<ThingPepe> clearDress(List<ThingPepe> dressList) {
        Iterator<ThingPepe> it = dressList.iterator();
        while (it.hasNext()) {
            ThingPepe thing = it.next();
            if (thing.getGallery().size() < 1 || thing.getColors().size() < 1) {
                System.out.println("Товар " + thing.getArticle() + " удален, нет картинки или цвета.");
                it.remove();
            } else {
                System.out.println("Товар " + thing.getArticle() + " добавлен " + Thing.counter + " из "/* + dressList.size()*/);
                Thing.counter++;
            }

            System.out.println(thing.toString());
        }
        return dressList;
    }
}

