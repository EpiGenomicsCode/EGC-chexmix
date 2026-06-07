package org.egc.core.utils;

import java.util.*;
import java.io.*;

/**
 * Utility class for parsing command-line arguments. Supports strings, integers,
 * doubles, longs, floats, flags, and file handles. Results are cached by args reference.
 */
public class Args {

    private static Map<String[], Set<String>> flags     = new HashMap<String[], Set<String>>();
    private static Map<String[], Set<String>> arguments = new HashMap<String[], Set<String>>();

    /** Returns all {@code --} prefixed argument names present in args. */
    public static Set<String> parseArgs(String args[]) {
        if (arguments.containsKey(args)) {
            return arguments.get(args);
        }
        HashSet<String> output = new HashSet<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].matches("^--.*")) {
                output.add(args[i].substring(2));
            }
        }
        arguments.put(args, output);
        return output;
    }

    /** Returns flags — {@code --foo} options that take no value. */
    public static Set<String> parseFlags(String args[]) {
        if (flags.containsKey(args)) {
            return flags.get(args);
        }
        HashSet<String> output = new HashSet<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].matches("^--.*") &&
                ((i == args.length - 1) || args[i+1].matches("^--.*"))) {
                output.add(args[i].substring(2));
            }
        }
        flags.put(args, output);
        return output;
    }

    public static int parseInteger(String args[], String key, int defaultValue) {
        if (!key.matches("^\\-\\-.*")) { key = "--" + key; }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(key)) { return Integer.parseInt(args[++i]); }
        }
        return defaultValue;
    }

    public static Collection<Integer> parseIntegers(String args[], String key) {
        ArrayList<Integer> output = new ArrayList<Integer>();
        if (!key.matches("^\\-\\-.*")) { key = "--" + key; }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(key)) { output.add(Integer.valueOf(args[++i])); }
        }
        return output;
    }

    public static long parseLong(String args[], String key, long defaultValue) {
        if (!key.matches("^\\-\\-.*")) { key = "--" + key; }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(key)) { return Long.parseLong(args[++i]); }
        }
        return defaultValue;
    }

    public static double parseDouble(String args[], String key, double defaultValue) {
        if (!key.matches("^\\-\\-.*")) { key = "--" + key; }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(key)) { return Double.parseDouble(args[++i]); }
        }
        return defaultValue;
    }

    public static Collection<Double> parseDoubles(String args[], String key) {
        ArrayList<Double> output = new ArrayList<Double>();
        if (!key.matches("^\\-\\-.*")) { key = "--" + key; }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(key)) { output.add(Double.valueOf(args[++i])); }
        }
        return output;
    }

    public static float parseFloat(String args[], String key, float defaultValue) {
        if (!key.matches("^\\-\\-.*")) { key = "--" + key; }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(key)) { return Float.parseFloat(args[++i]); }
        }
        return defaultValue;
    }

    public static String parseString(String args[], String key, String defaultValue) {
        if (!key.matches("^\\-\\-.*")) { key = "--" + key; }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(key)) { return args[++i]; }
        }
        return defaultValue;
    }

    public static Collection<String> parseStrings(String args[], String key) {
        ArrayList<String> output = new ArrayList<String>();
        if (!key.matches("^\\-\\-.*")) { key = "--" + key; }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(key)) { output.add(args[++i]); }
        }
        return output;
    }

    public static List<String> parseFile(String args[]) {
        ArrayList<String> output = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--file")) { output.add(args[++i]); }
            if (args[i].equals("--")) {
                for (int j = i + 1; j < args.length; j++) { output.add(args[j]); }
                break;
            }
        }
        return output;
    }

    public static List<File> parseFileHandles(String args[], String key) {
        if (!key.matches("^\\-\\-.*")) { key = "--" + key; }
        ArrayList<File> output = new ArrayList<File>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(key)) { output.add(new File(args[++i])); }
        }
        return output;
    }

    public static List<String> parseList(String args[], String key) {
        if (!key.matches("^\\-\\-.*")) { key = "--" + key; }
        ArrayList<String> output = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(key)) { output.add(args[++i]); }
        }
        return output;
    }
}
