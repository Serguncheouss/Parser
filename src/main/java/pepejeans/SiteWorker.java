package pepejeans;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.gistlabs.mechanize.document.html.HtmlDocument;
import com.gistlabs.mechanize.document.node.Node;
import com.gistlabs.mechanize.exceptions.MechanizeException;
import com.gistlabs.mechanize.impl.MechanizeAgent;

/**
 * Created by Strel on 06.08.2016.
 */
public class SiteWorker {
    /** Логин */
    private String login;
    /** Пароль */
    private String password;
    /** Ссылка на сайт */
    private String siteUrl = "http://webtool.pepejeans.com";
    /** Ссылка на страницу авторизацию */
    private String accountUrl;
    /** Ссылка на начальную страницу сайта */
    private String startUrl = "http://webtool.pepejeans.com/pjlweb/index.php";
    /** Название файла с данными авторизации */
    private String accountDataFile = "accountPepe.txt";
    /** Создание объекта парсера MechanizeAgent */
    private MechanizeAgent agent = new MechanizeAgent();
    /** Название и ссылка на выбранную коллекцию */
    private Map.Entry collection;
    /** Название и ссылка на выбранный брэнд */
    private Map.Entry brand;
    /** Название и ссылка на выбранный раздел */
    private Map.Entry division;
    /** Название и ссылка на выбранную тему */
    private Map.Entry theme;
    private HtmlDocument dressPage;
    private List<ThingPepe> dress = new ArrayList<>();
    private static final int TEST_END_COUNT = 3; // Количество тестовых итераций
    private int testCounter = 0; // Счетчик тестовых итераций

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
            login();
            collection = getCollection(getPage(startUrl));
            brand = getBrand(getPage(startUrl + collection.getValue()));
            division = getDivision(getPage(brand.getValue().toString()));
            theme = getTheme(getPage(division.getValue().toString()));
            dressPage = getPage(theme.getValue().toString());
        }
        catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
    public static void main(String[] args) throws IOException {
        SiteWorker siteWorker = new SiteWorker();
        siteWorker.parseDress();
        siteWorker.getThings();
        //siteWorker.findThing("PM200143M84"); // для теста
    }
    private void login() {
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
        }
        return page;
    }
    private String getInput(List<String> list) throws IOException {
        System.out.print("> ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int choice;
        while (true) {
            try {
                choice = Integer.parseInt(reader.readLine());
                if (choice < 1 || choice > list.size()) {
                    System.out.println("Такого пункта нет. Повторите ввод.");
                } else break;
            } catch (NumberFormatException e) {
                System.out.println("Вы ввели не цифру, повторите ввод.");
            }
        }

        return list.get(choice - 1);
    }
    private Map.Entry<String, String> getCollection(HtmlDocument page) throws IOException {
        Map<String,String> collections = new HashMap<>();
        Map.Entry<String,String> choiceCollection = null;
        List<? extends Node> collectionList = page.find("#collection_list").findAll("a");
        collectionList.forEach((collection) -> collections.put(collection.getAttribute("${innerHtml}"), collection.getAttribute("href")));
        for (int i = 0; i < collectionList.size(); i++) {
            System.out.println((i + 1) + ". " + collectionList.get(i).getAttribute("${innerHtml}"));
        }

        List<String> collectionListPrepareToInput = collectionList.stream().map(collection -> collection.getAttribute("${innerHtml}")).collect(Collectors.toList());

        System.out.println("Выберите коллекцию:");
        String input = getInput(collectionListPrepareToInput);

        for (Map.Entry<String, String> entry : collections.entrySet()) { // Как это заменит на лямбда выражение
            if(entry.getKey().equals(input))
                choiceCollection = entry;
        }

        return choiceCollection;
    }
    private Map.Entry<String, String> getBrand(HtmlDocument page) throws IOException {
        Map<String,String> brands = new HashMap<>();
        Map.Entry<String,String> choiceBrand = null;
        List<? extends Node> brandList = page.find("#listado_brands").findAll("li");
        brands.put(brandList.get(0).getAttribute("${innerHtml}"), page.getUri());
        brandList.stream().skip(1).forEach((brand) -> brands.put(brand.find("a").getAttribute("${innerHtml}"), startUrl + brand.find("a").getAttribute("href")));
        for (int i = 0; i < brandList.size(); i++) {
            if(i > 0)
                System.out.println((i + 1) + ". " + brandList.get(i).find("a").getAttribute("${innerHtml}"));
            else
                System.out.println((i + 1) + ". " + brandList.get(i).getAttribute("${innerHtml}"));
        }

        List<String> brandListPrepareToInput = brandList.stream().map(brand -> {
            if(brand.hasAttribute("a"))
                return brand.find("a").getAttribute("${innerHtml}");
            else
                return brand.getAttribute("${innerHtml}");
        }).collect(Collectors.toList());

        System.out.println("Выберите брэнд:");
        String input = getInput(brandListPrepareToInput);

        for (Map.Entry<String, String> entry : brands.entrySet()) { // Как это заменить на лямбда выражение
            if(entry.getKey().equals(input))
                choiceBrand = entry;
        }

        return choiceBrand;
    }
    private Map.Entry<String, String> getDivision(HtmlDocument page) throws IOException {
        Map<String,String> divisions = new HashMap<>();
        Map.Entry<String,String> choiceDivision = null;
        List<? extends Node> divisionList = page.find("#listado_divisiones").findAll("li");
        divisions.put(divisionList.get(0).getAttribute("${innerHtml}"), page.getUri());
        divisionList.stream().skip(1).forEach((division) -> divisions.put(division.find("a").getAttribute("${innerHtml}"), startUrl + division.find("a").getAttribute("href")));
        for (int i = 0; i < divisionList.size(); i++) {
            if(i > 0)
                System.out.println((i + 1) + ". " + divisionList.get(i).find("a").getAttribute("${innerHtml}"));
            else
                System.out.println((i + 1) + ". " + divisionList.get(i).getAttribute("${innerHtml}"));
        }

        List<String> divisionListPrepareToInput = divisionList.stream().map(division -> {
            if(division.hasAttribute("a"))
                return division.find("a").getAttribute("${innerHtml}");
            else
                return division.getAttribute("${innerHtml}");
        }).collect(Collectors.toList());

        System.out.println("Выберите раздел:");
        String input = getInput(divisionListPrepareToInput);

        for (Map.Entry<String, String> entry : divisions.entrySet()) { // Как это заменить на лямбда выражение
            if(entry.getKey().equals(input))
                choiceDivision = entry;
        }

        return choiceDivision;
    }
    private Map.Entry<String, String> getTheme(HtmlDocument page) throws IOException { // доделать
        Map<String,String> themes = new HashMap<>();
        Map.Entry<String,String> choiceTheme = null;
        List<? extends Node> themeList = page.find("#listado_themes").findAll("a");
        themes.put(themeList.get(0).find("span").getAttribute("${innerHtml}"), page.getUri());
        themeList.forEach((theme) -> themes.put(theme.find("a").find("span").getAttribute("${innerHtml}"), startUrl + theme.find("a").getAttribute("href")));
        for (int i = 0; i < themeList.size(); i++) {
            System.out.println((i + 1) + ". " + themeList.get(i).find("span").getAttribute("${innerHtml}"));
        }

        List<String> themeListPrepareToInput = themeList.stream().map(theme -> theme.find("span").getAttribute("${innerHtml}")).collect(Collectors.toList());

        System.out.println("Выберите тему:");
        String input = getInput(themeListPrepareToInput);

        for (Map.Entry<String, String> entry : themes.entrySet()) { // Как это заменить на лямбда выражение
            if(entry.getKey().equals(input))
                choiceTheme = entry;
        }

        return choiceTheme;
    }
    private void parseDress() {
        int thingCounter = 1;
        ThingPepe tThing;
        for (Node node : dressPage.find("div#linebook_list").findAll("li")) {
            if (TEST_END_COUNT != 0 && testCounter >= TEST_END_COUNT) break; // test
            Node li = node;
            if (
                    !li.find("span.images").find("img").getAttribute("src").contains("not_available") & // убираем недоступные товары
                            !li.find("span.images").find("img").getAttribute("src").contains("cancelled") & // убираем отмененные товары
                            li.find("span.offeronly") == null // убираем товары только для предложения
                    ) {
                dress.add(tThing = new ThingPepe( // добавляем новую вещь в массив
                        li.getAttribute("id").substring(li.getAttribute("id").indexOf("_") + 1), // артикул
                        li.find("em").find("span.stylename").getValue(), // имя
                        li.find("a").getAttribute("href") // ссылка
                ));
                getParamsForThing(tThing);
                System.out.println("Товар добавлен " + thingCounter + " из " + dressPage.find("div#linebook_list").findAll("li").size());
                testCounter++; // test
            }
            thingCounter++;
        }

//          Пока заменил на итератор, посмотрим на функционал и производительность
//        dressPage.findAll("ul.gallery").forEach(ul -> ul.findAll("li").forEach(li -> {
//            if (
//                    !li.find("span.images").find("img").getAttribute("src").contains("not_available") & // убираем недоступные товары
//                            !li.find("span.images").find("img").getAttribute("src").contains("cancelled") & // убираем отмененные товары
//                            li.find("span.offeronly") ==  null // убираем товары только для предложения
//                    )
//            {
//                dress.add(new ThingPepe( // добавляем новую вещь в массив
//                        li.getAttribute("id").substring(li.getAttribute("id").indexOf("_") + 1), // артикул
//                        li.find("em").find("span.stylename").getValue(), // имя
//                        li.find("a").getAttribute("href") // ссылка
//                ));
//                System.out.println("Товар добавлен");
//            }
//        }));
        System.out.println("Добавлено " + dress.size() + " вещей");
    }
    /** Парсит параметры для товара */
    private void getParamsForThing (ThingPepe thing) {
        String attrName;
        HtmlDocument page = getPage(startUrl + thing.getUrl());
        // <-- Парсим картинки
        Iterator<? extends Node> it = page.findAll("div.images").iterator();
        String tUrl;
        while (it.hasNext()) {
            Node image = it.next();
            tUrl = image.find("a").getAttribute("href");
            thing.addImageToGallery(siteUrl + tUrl.substring(tUrl.indexOf("imagen=") + 7));
        }
        // --> Парсим картинки
        // <-- Парсим цвета
        it = page.findAll("div.color").iterator();
        if (!it.hasNext()) {
            String colorId = page.find("div#wash_code").find("div.name").getValue();
            String colorUrl = siteUrl + page.find("div#wash_code").find("img").getAttribute("src");
            thing.addColor(colorId, "", colorUrl);
        }
        while (it.hasNext()) {
            String colorId = it.next().find("div.name").getValue();
            String colorName = it.next().find("div.small").getAttribute("title");
            String colorUrl = siteUrl + it.next().find("img").getAttribute("src");
            thing.addColor(colorId, colorName, colorUrl);
        }
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
    /** Выводит массив вещей на экран */
    public void getThings() {
        dress.forEach(System.out::println);
    }
    /** Ищет определенную вещь и выводит на экран */
    public void findThing(String id) {
        System.out.println(dress.stream().filter(thingPepe -> thingPepe.getArticle().equals(id)).findFirst().orElse(null));
    }
    public List<ThingPepe> parse() {
        parseDress();
        return dress;
    }
}

