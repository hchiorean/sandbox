package org.test.news;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mock implementation of a logger
 * 
 * @author Horia Chiorean (horia.chiorean@gmail.com)
 */
public class Logger {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:SSS");
    private final boolean print;
    
    protected Logger(boolean print) {
        this.print = print;
    }
    
    protected void log(String message, Object...args) {
        if (!print) {
            return;
        }
        String formatted = "%s %s " + message + " %n";
        Object[] newArgs = new Object[args.length + 2];
        newArgs[0] = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        newArgs[1] = Thread.currentThread().getName();
        System.arraycopy(args, 0, newArgs, 2, args.length);
        System.out.printf(formatted, newArgs);
    }  
    
    protected void log(Throwable t) {
        if (!print) {
            return;
        }
        t.printStackTrace();
    }
}
