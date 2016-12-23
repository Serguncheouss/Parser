package ru.greus.parser;

import ru.greus.parser.pepejeans.SiteWorker;
import ru.greus.parser.pepejeans.ThingPepe;
import ru.greus.parser.pepejeans.XLSWorker;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by Strel on 19.12.2016.
 */
public class Launcher {
    public static void main(String []args) {
        SiteWorker site = new SiteWorker();
        XLSWorker excel = new XLSWorker("Orderform AW17PC - клиент.xlsx", 1,7);
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
