package com.xebia.functional.xef.java.auto.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {

    private static final BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Read line from the console (IDE friendly)
     * @return
     */
    public static String readLine() {
        try {
            return sysin.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
