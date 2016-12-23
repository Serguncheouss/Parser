package ru.greus.parser;

import ru.greus.parser.pepejeans.SiteWorker;
import ru.greus.parser.pepejeans.ThingPepe;
import ru.greus.parser.pepejeans.XLSWorker;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

/**
 * Created by Strel on 19.12.2016.
 */
public class Launcher {
    public static void main(String []args) throws InterruptedException {
        XLSWorker excel = null;
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
            }
        } catch (Exception e) {
            System.exit(1);
            Thread. sleep(5000);
        }

        SiteWorker site = new SiteWorker();

        List<ThingPepe> dress = excel.parse(site.parse());

        try(FileWriter fw = new FileWriter("result.txt")) {
            String text = "";
            for (ThingPepe thing : dress) {
                text += thing.toSP();
            }
            fw.write(text);
            fw.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
