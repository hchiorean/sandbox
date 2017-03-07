package org.test.news;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * A feed which sends words periodically to a {@link Analyzer} based on the rules from {@link MessageGenerator}
 *
 * @author Horia Chiorean (horia.chiorean@gmail.com)
 */
public class Feed {
    
    private static final Logger LOGGER = new Logger(false);
    
    private final int numberOfItems;
    private final long frequencyMillis;
    private final int port;
    private final InetAddress host;
    private final MessageGenerator messageGenerator;
    
    public Feed(int numberOfItems, int frequency, InetAddress host, int port) {
        this.numberOfItems = numberOfItems;
        this.frequencyMillis = TimeUnit.SECONDS.toMillis(frequency);
        this.port = port;
        this.host = host;
        this.messageGenerator = new MessageGenerator(3, 5);
    }
    
    public void start() {
        int retryAttempt = 3;
        boolean connectSuccessful = false;
        while (retryAttempt >= 0 && !connectSuccessful) {
            try (Socket socket = new Socket(host, port); OutputStream outputStream = socket.getOutputStream();
                 PrintWriter writer = new PrintWriter(outputStream, true)) {
                socket.setKeepAlive(true);
                connectSuccessful = true;
                System.out.printf("Connected to server %s:%d %n", host, port);
                System.out.printf("Sending %d messages every %d seconds", numberOfItems, frequencyMillis / 1000);
                while (!Thread.currentThread().isInterrupted()) {
                    long start = System.currentTimeMillis();
                    while (System.currentTimeMillis() - start < frequencyMillis) {
                        //spin wait...                      
                        Thread.sleep(100);
                    }
                    System.out.printf("sending %d messages to server %n", numberOfItems);
                    messageGenerator.generateMessages(numberOfItems, message -> {
                        LOGGER.log("sent message %s", message);
                        writer.println(message);
                    });
                }
            } catch (IOException e) {
                System.out.printf("Cannot connect to server %s:%d; reason: %s %n", host, port, e.getMessage());
                if (--retryAttempt >= 0) {
                    try {
                        System.out.println("retrying...");
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        Thread.interrupted();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Stopping due to interrupt request");
                Thread.interrupted();
            }
        }
    }
    
    public static void main(String[] args) {
        
        String port = "8913";
        int numberOfItems = 100;
        int frequency = 10;
        String hostName = "localhost";
        InetAddress host;
        int argsCount = args.length;
        System.out.printf("Usage: Feed [number_of_items:%d] [frequency_seconds:%d] [host:%s] [port:%s] %n", numberOfItems,
                          frequency, hostName, port);
        if (argsCount < 2) {
            System.out.printf("...attempting to connect to %s:%s %n", hostName, port);
        }
        try {
            numberOfItems = argsCount >= 1 ? Integer.valueOf(args[0]) : numberOfItems;
            frequency = argsCount >= 2 ? Integer.valueOf(args[1]) : frequency;
            if (argsCount > 2) {
                hostName = args[2];
                host = InetAddress.getByName(hostName);
            } else {
                host = InetAddress.getLocalHost();
            }
            port = argsCount > 3 ? args[3] : port;
            int portNr = Integer.valueOf(port);
            Feed feed = new Feed(numberOfItems, frequency, host, portNr);
            feed.start();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input: " + e.getMessage());
            System.exit(-1);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + hostName);
            System.exit(-1);
        }
    }
}
