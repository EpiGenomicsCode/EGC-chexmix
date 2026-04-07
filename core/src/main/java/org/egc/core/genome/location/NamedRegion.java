package org.egc.core.genome.location;

import org.egc.core.genome.Genome;

public class NamedRegion extends Region implements Named {
    private String name;
    
    public NamedRegion(NamedRegion nr) { 
        super(nr);
        name = nr.name;
    }
    
    public NamedRegion(Region r) { 
        super(r);
        name = r.getLocationString();
        if(r instanceof NamedRegion) { 
            name = ((NamedRegion)r).name;
        }
    }
    
    public NamedRegion(Region r, String n) {
        super(r);
        name = n;
    }
    
    public NamedRegion(Genome g, String c, int start, int end, String name) {
        super(g,c,start,end);
        this.name = name;
    }
    
    public void setName(String n) { name = n; }

    public String getName() {return name;}
    public String toString() {return getName();}
    
    public boolean equals(Object o) { 
        if(!(o instanceof NamedRegion)) { return false; }
        NamedRegion nr = (NamedRegion)o;
        if(!name.equals(nr.name)) { return false; }
        return super.equals(o);
    }
    
    public int hashCode() { 
        int code = super.hashCode();
        code += name.hashCode(); code *= 37;
        return code; 
    }
}
