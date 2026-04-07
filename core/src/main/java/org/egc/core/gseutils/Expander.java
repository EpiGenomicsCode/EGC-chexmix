package org.egc.core.gseutils;

import java.util.Iterator;

/* An Expander<A,B> maps an object of type A to a set of objects of type
   B (represented as an Iterator<B>) */

public interface Expander<A,B> {

    public Iterator<B> execute(A a);
}
