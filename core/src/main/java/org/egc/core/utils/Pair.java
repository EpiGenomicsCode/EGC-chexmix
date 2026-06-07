package org.egc.core.utils;

import java.util.Objects;

public final class Pair<A,B> {

    private final A first;
    private final B last;

    public Pair(A first, B last) {
        this.first = first;
        this.last  = last;
    }

    public A first() { return first; }
    public B last()  { return last;  }

    public Pair<B,A> swap() { return new Pair<>(last, first); }

    @Override
    public String toString() { return "<" + first + "," + last + ">"; }

    @Override
    public int hashCode() {
        return 37 * Objects.hashCode(first) + Objects.hashCode(last);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair<?,?> p = (Pair<?,?>) o;
        return Objects.equals(first, p.first) && Objects.equals(last, p.last);
    }
}
