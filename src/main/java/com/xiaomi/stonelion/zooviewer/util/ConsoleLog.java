package com.xiaomi.stonelion.zooviewer.util;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.fusesource.jansi.Ansi.ansi;

public class ConsoleLog {
    public static enum Color {
        RED,
        GREEN,
        YELLOW,
        BLUE,
        MAGENTA,
        CYAN,
        WHITE
    }

    static {
        AnsiConsole.systemInstall();
    }

    public static void printException(Throwable t) {
        StringWriter out = new StringWriter();
        PrintWriter p = new PrintWriter(out);
        t.printStackTrace(p);
        println(Color.RED, out.toString());
    }

    public static void print(Color color, Object text) {
        System.out.print(ansi().fg(mapColor(color)).a(text).reset());
    }

    public static void println(Color color, Object text) {
        System.out.println(ansi().fg(mapColor(color)).a(text).reset());
    }

    private static Ansi.Color mapColor(Color c) {
        switch (c) {
            case RED:
                return Ansi.Color.RED;
            case GREEN:
                return Ansi.Color.GREEN;
            case YELLOW:
                return Ansi.Color.YELLOW;
            case BLUE:
                return Ansi.Color.BLUE;
            case MAGENTA:
                return Ansi.Color.MAGENTA;
            case CYAN:
                return Ansi.Color.CYAN;
            case WHITE:
                return Ansi.Color.WHITE;
            default:
                return Ansi.Color.DEFAULT;
        }
    }
}
