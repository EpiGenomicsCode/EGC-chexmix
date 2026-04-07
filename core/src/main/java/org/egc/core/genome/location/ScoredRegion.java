package org.egc.core.genome.location;

import org.egc.core.genome.Genome;


public class ScoredRegion extends Region implements Scored {
    protected double score;
    
    public ScoredRegion(ScoredRegion copied) { 
        super(copied);
        score = copied.score;
    }
    
    public ScoredRegion (Genome g, String c, int start, int end, double score) {
        super(g,c,start,end);
        this.score = score;
    }

    public double getScore() {return score;}

    public String toString() {
        return String.format("%s (%f)",
                             regionString(),
                             score);
    }

    public boolean equals(Object o) {
        if (o instanceof ScoredRegion) {
            ScoredRegion other = (ScoredRegion)o;
            return super.equals(other) && other.getScore() == getScore();
        } else {
            return false;
        }
    }
}

