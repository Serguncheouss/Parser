package ru.greus.parser;

import ru.greus.parser.pepejeans.SiteWorker;
import ru.greus.parser.pepejeans.ThingPepe;
import ru.greus.parser.pepejeans.XLSWorker;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Strel on 19.12.2016.
 */
public class Launcher {
    public static void main(String []args) throws InterruptedException {
        XLSWorker excel = null;
        try {
            excel = new XLSWorker("Orderform AW17PC - клиент.xlsx", 1, 7);
//            switch (args.length) {
//                case 0:
//                    excel = new XLSWorker();
//                    break;
//                case 1:
//                    excel = new XLSWorker(args[0]);
//                    break;
//                case 2:
//                    excel = new XLSWorker(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
//                    break;
//                case 3:
//                    excel = new XLSWorker(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
//                    break;
//                default:
//                    System.exit(1);
//            }
            SiteWorker site = new SiteWorker();
            List<ThingPepe> dress = site.parse(excel.parse());

            SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy_hh.mm");

            for (ThingPepe thing : dress) {
                thing.toSP(format1.format(new Date()));
            }
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
