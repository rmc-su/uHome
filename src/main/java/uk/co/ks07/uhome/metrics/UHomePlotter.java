package uk.co.ks07.uhome.metrics;

import uk.co.ks07.uhome.HomeList;

public abstract class UHomePlotter extends Metrics.Plotter {
    protected HomeList homeList;

    public UHomePlotter(String name, HomeList hl) {
        super(name);
        this.homeList = hl;
    }

    @Override
    public abstract int getValue();
}
