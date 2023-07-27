package com.xebia.functional.xef.java.auto.jdk8.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleUtil implements AutoCloseable{

    private final BufferedReader sysin;

    public ConsoleUtil(){
        sysin = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Read line from the console (IDE friendly)
     * @return line from console input
     */
    public String readLine() {
        try {
            return sysin.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        sysin.close();
    }
}
