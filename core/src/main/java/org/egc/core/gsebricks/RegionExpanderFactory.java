package org.egc.core.gsebricks;

import org.egc.core.genome.Genome;
import org.egc.core.genome.location.Region;
import org.egc.core.gsebricks.verbs.Expander;

public interface RegionExpanderFactory<PRODUCT> {
    public void setType(String type);    
    public String getType();
    public String getProduct();
    public Expander<Region,PRODUCT> getExpander(Genome g);
    public Expander<Region,PRODUCT> getExpander(Genome g, String type);
}
