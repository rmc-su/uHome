package uk.co.ks07.uhome.griefcraft;

import uk.co.ks07.uhome.HomeList;

public abstract class UHomePlotter extends Metrics.Plotter {
    protected HomeList homeList;

    public UHomePlotter(String name, HomeList hl) {
        this.homeList = hl;
    }

    public abstract int getValue();
}
