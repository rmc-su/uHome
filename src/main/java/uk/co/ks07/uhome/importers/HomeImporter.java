package uk.co.ks07.uhome.importers;

import uk.co.ks07.uhome.HomeList;

public interface HomeImporter {

    public boolean canImport();
    
    public void tryImport(HomeList hL);
}
