package org.egc.core.genome.sequence;

/**
 * <code>SequenceUtils</code> provides a number of static methods for manipulating
 * DNA sequences stores as strings or char[].
 *
 * @author <a href="mailto:arolfe@mit.edu">Alex Rolfe</a>
 */
public class SequenceUtils {

    /**
     * <code>complement</code> returns the complement of a nucleotide in the 2-bit representation.
     */
    public static int complement(int i) {
    	switch(i) {
    	case 0: return 1;
    	case 1: return 0;
    	case 2: return 3;
    	case 3: return 2;
    	default: return -1;
    	}
    }

    /**
     * <code>complement</code> returns the complement of a nucleotide in the character representation
     * (A,C,T,G,a,c,t,g).
     */
    public static char complementChar(char c) {
        if (trans == null) {
            trans = new char['z'];
            trans['A'] = 'T';
            trans['C'] = 'G';
            trans['T'] = 'A';
            trans['G'] = 'C';
            trans['a'] = 'T';
            trans['c'] = 'G';
            trans['t'] = 'A';
            trans['g'] = 'C';
            trans['n'] = 'N';
            trans['N'] = 'N';
        }
        return trans[c];
    }

    private static char[] trans;

    /**
     * <code>reverseComplement</code> mutates in the input array of characters
     * (A,C,T,G,a,c,t,g) to be the reverse complement.
     */
    public static void reverseComplement(char[] array) {
        if (trans == null) {
            trans = new char['z'];
            trans['A'] = 'T';
            trans['C'] = 'G';
            trans['T'] = 'A';
            trans['G'] = 'C';
            trans['a'] = 't';
            trans['c'] = 'g';
            trans['t'] = 'a';
            trans['g'] = 'c';
            trans['N'] = 'N';
            trans['n'] = 'n';
            trans['X'] = 'X';
            trans['x'] = 'x';
        }
        int i;
        int end = array.length - 1;
        for (i = 0; i <= array.length / 2 && i < array.length; i++) {
            try {
                char first = array[i];
                array[i] = trans[array[end - i]];
                array[end-i] = trans[first];
            } catch (ArrayIndexOutOfBoundsException ex) {
                ex.printStackTrace();
                System.err.println("i=" + i);
                System.err.println("first = " + array[i]);
                System.err.println("other = " + array[end-i]);
                System.err.println("trans = " + trans[array[i]] + " and " + trans[array[end-i]]);
            }
        }
    }

    public static String reverseComplement(String str) {
		StringBuilder sb = new StringBuilder();
		for(int i = str.length()-1; i>= 0; i--) {
			sb.append(complementChar(str.charAt(i)));
		}
		return sb.toString();
	}

    public static byte[] reverseComplement(byte[] bases) {
		byte[] rc = new byte[bases.length];
		int j=0;
		for(int i = bases.length-1; i>= 0; i--) {
			rc[j]=(byte)complementChar((char)bases[i]);
			j++;
		}
		return rc;
	}

    /** converts from 2-bit representation to character representation */
    public static char int2char(int i) {
        switch(i) {
        case 0: return 'A';
        case 1: return 'C';
        case 2: return 'G';
        case 3: return 'T';
        }
        return 'n';
    }

    /** converts from character representation to 2-bit representation */
    public static int char2int(char c) {
        switch(c) {
        case 'a':
        case 'A':
            return 0;
        case 'c':
        case 'C':
            return 1;
        case 'g':
        case 'G':
            return 2;
        case 't':
        case 'T':
            return 3;
        }
        return -1;
    }
}
