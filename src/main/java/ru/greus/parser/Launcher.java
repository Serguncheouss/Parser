package ru.greus.parser;

import ru.greus.parser.pepejeans.SiteWorker;
import ru.greus.parser.pepejeans.ThingPepe;
import ru.greus.parser.pepejeans.XLSWorker;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Strel on 19.12.2016.
 */
public class Launcher {
    private static final int THREADS_COUNT = 15;
    public static void main(String []args) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS_COUNT);
        List<Future<SiteWorker>> handles = new ArrayList<>();
        Future<SiteWorker> handle;
        int dressCount = 0;
        Long sTime, eTime;
        XLSWorker excel = null;
        List<ThingPepe> dress = null;
        sTime = System.currentTimeMillis();
        try {
            switch (args.length) {
                case 0:
                    excel = new XLSWorker();
                    break;
                case 1:
                    excel = new XLSWorker(args[0]);
                    break;
                case 2:
                    excel = new XLSWorker(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    break;
                case 3:
                    excel = new XLSWorker(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                    break;
                default:
                    System.exit(1);
            }
            dress = excel.parse();
        } catch (IOException e) {
            System.out.println("[Error] - Невозможно открыть прайс: "+ e.getMessage());
            System.out.println("Нажмите любую ввод...");
            System.exit(1);
        }
        eTime = System.currentTimeMillis();

        System.out.println("[Total] - Всего добавлено товаров из прайса: " + dress.size() + " шт. за " +
                ((eTime - sTime) / 1000 / 60) + " мин. " + ((eTime - sTime) / 1000 % 60) + " сек.");

        sTime = System.currentTimeMillis();

        for (ThingPepe thing : dress) {
            handle = executorService.submit(new SiteWorker(thing, dressCount + 1, dress.size()));
            handles.add(handle);
            dressCount++;
        }

        for (Future<SiteWorker> h : handles) {
            try {
                h.get();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        executorService.shutdownNow();

        Iterator<ThingPepe> it = dress.iterator();
        while (it.hasNext()) {
            ThingPepe thing = it.next();
            if (thing.getGallery().size() < 1 || thing.getColors().size() < 1) {
                System.out.println("[Delete] - Товар " + thing.getArticle() + " удален, нет картинки или цвета.");
                it.remove();
            }
        }

        eTime = System.currentTimeMillis();
        System.out.println("[Total] - Всего добавлено товаров с сайта: " + dress.size() + " шт. за " +
                ((eTime - sTime) / 1000 / 60) + " мин. " + ((eTime - sTime) / 1000 % 60) + " сек.");

        System.out.println("[Try] - Создание и запись файлов...");
        SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy_hh.mm");

        try {
            List<String> fileNames = new ArrayList<>();
            for (ThingPepe thing : dress) {
                String fileName = thing.toSP(format1.format(new Date()));
                if (!fileNames.contains(fileName)) {
                    System.out.println("[Ok] - Создан файл: " + fileName);
                    fileNames.add(fileName);
                }
            }
            System.out.println("Нажмите любую ввод...");
            System.in.read();
        } catch (IOException e) {
            System.out.println("[Error] - Невозможно записать файл: " + e.getMessage());
            try {
                System.out.println("Нажмите любую ввод...");
                System.in.read();
            } catch (IOException ignored) {
            }
        }
    }
}
