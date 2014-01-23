package com.xiaomi.stonelion.miliao;


import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: shixin
 * Date: 1/9/14
 * Time: 11:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class HelloWorld {
    private static final Logger logger = Logger.getLogger(HelloWorld.class);

    private static final String HELLO_WORLD_FILE = "hello_world.txt";

    public static void main(String[] args) throws IOException {
        System.out.print("System.out says hi!");

        String line = null;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(HELLO_WORLD_FILE)));

            while ((line = bufferedReader.readLine()) != null) {
                logger.info("hello_world.txt says " + line);
            }

            logger.info("Ok, bye.");
        } catch (FileNotFoundException e) {
            logger.error(e);
        } finally {
            if (null != bufferedReader) {
                bufferedReader.close();
            }
        }
    }
}
