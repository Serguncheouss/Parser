import pepejeans.SiteWorker;
import pepejeans.XLSWorker;

/**
 * Created by Strel on 19.12.2016.
 */
public class Launcher {
    public static void main(String []args) {
        SiteWorker site = new SiteWorker();
        XLSWorker excel = new XLSWorker();
        System.out.println(excel.parse(site.parse()));
    }
}
