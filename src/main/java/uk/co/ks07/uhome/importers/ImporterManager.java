package uk.co.ks07.uhome.importers;

import uk.co.ks07.uhome.HomeList;
import uk.co.ks07.uhome.uHome;

public class ImporterManager {
    private final uHome uH;
    private final HomeList hL;

    public ImporterManager(uHome uH, HomeList homeList) {
        this.uH = uH;
        this.hL = homeList;
    }

    public void checkImports() {
        for (HomeImporter imp : this.getAllImporters()) {
            if (imp.canImport()) {
                imp.tryImport(hL);
            }
        }
    }

    public HomeImporter[] getAllImporters() {
        HomeImporter[] ret = new HomeImporter[2];

        ret[0] = new CommandBookImporter(uH);
        ret[1] = new MultiHomesImporter(uH);
        ret[2] = new MultipleHomesImporter(uH);

        return ret;
    }
}
