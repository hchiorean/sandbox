package org.test.news;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Queue;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Server which analyzes incoming messages for content and displays some headline information
 * 
 * @author Horia Chiorean (horia.chiorean@gmail.com)
 */
public class Analyzer {
    
    private static final int POOL_CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int POOL_MAX_SIZE = 4 * POOL_CORE_COUNT; //arbitrary for now...
    private static final Logger LOGGER = new Logger(false);
    
    private final int port;
    private final ExecutorService socketProcessingService;
    private final ScheduledExecutorService reportingService;
    private final ConcurrentLinkedQueue<NewsItem> newsItems;
    
    public Analyzer(int port) {
        if (port <= 0) {
            throw new IllegalStateException("You must provide a positive port");
        }
        this.newsItems = new ConcurrentLinkedQueue<>();
        this.port = port;
        this.socketProcessingService = new ThreadPoolExecutor(POOL_CORE_COUNT, POOL_MAX_SIZE, 30, TimeUnit.SECONDS,
                                                              new LinkedBlockingQueue<>(),
                                                              namedThreadFactory("news-feed-task"),
                                                              (r, executor) -> {
                                                                  //explain the rejection
                                                                  System.out.printf(
                                                                          "Cannot add any more client processing threads because the server is full running %d tasks; retry later or increase pool size %n",
                                                                          executor.getActiveCount());
                                                      });
       this.reportingService = new ScheduledThreadPoolExecutor(1, namedThreadFactory("reporting-task"));
       long timeInterval = 10;
       TimeUnit timeUnit = TimeUnit.SECONDS;
       this.reportingService.scheduleAtFixedRate(() -> processHeadlines(timeInterval, timeUnit), timeInterval, timeInterval, timeUnit);
        
    }
    
    private void processHeadlines(long timeInterval, TimeUnit timeUnit) {
        if (newsItems.isEmpty()) {
            System.out.printf("No positive new news items found during the last %d %s %n %n", timeInterval, timeUnit.toString());
            return;
        }
        TreeSet<NewsItem> sortedSnapshot = new TreeSet<>(); //sorted in reverse order (highest first)
        NewsItem newsItem;
        while ((newsItem = newsItems.poll()) != null) {
            sortedSnapshot.add(newsItem);
        }
        System.out.printf("Found %d positive items during the last %d %s %n %n", sortedSnapshot.size(), timeInterval, timeUnit.toString());
        int numberOfTopElements = 3;
        String headline = sortedSnapshot.stream()
                                        .limit(numberOfTopElements)
                                        .map(NewsItem::headline)
                                        .collect(Collectors.joining(System.lineSeparator()));
        System.out.println(headline);
    }
    
    private ThreadFactory namedThreadFactory(String name) {
        return runnable -> new Thread(runnable, name);
    }
    
    protected void start() {
        System.out.printf("Starting analyzer service on %d %n" , port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                socket.setKeepAlive(true);
                String feedId = UUID.randomUUID().toString();
                FeedProcessingTask processingTask = new FeedProcessingTask(socket, newsItemProducer(feedId), newsItems);
                socketProcessingService.submit(processingTask);
            }
        } catch (IOException e) {
            System.out.printf("Cannot start socket server on port %d %n", port);
            LOGGER.log(e);
        } finally {
            socketProcessingService.shutdownNow();
            reportingService.shutdownNow();
        }
    
    }
    
    private Function<String, NewsItem> newsItemProducer(String feedId) {
        return line -> {
            Objects.requireNonNull(line, "received empty message");
            // take the priority as the last item from the message line
            try {
                int lastCharIdx = line.length() - 1;
                String headline = line.substring(0, lastCharIdx);
                Byte prio = Byte.valueOf(line.substring(lastCharIdx));
                NewsItem item = new NewsItem(prio, headline, feedId);
                boolean isPositive = PhraseScorer.INSTANCE.scoreMessage(headline) > 0;
                if (!isPositive) {
                    LOGGER.log("dropping %s because it's not positive", line);
                }
                return isPositive ? item : null;
            } catch (IllegalArgumentException e) {
                LOGGER.log("discarding message %s because it is not correctly formed; expected <headline> <prio>", line);
                return null;
            }
        };
    }
    
    /**
     * Thread processing each new client connection
     */
    private static class FeedProcessingTask implements Callable<Void> {
        private final Socket socket;
        private final Function<String, NewsItem> newsItemProducer;
        private final Queue<NewsItem> queue;
    
        protected FeedProcessingTask(Socket socket, Function<String, NewsItem> newsItemProducer, Queue<NewsItem> queue) {
            this.socket = socket;
            this.newsItemProducer = newsItemProducer;
            this.queue = queue;
            LOGGER.log("received client on %s", socket.toString());
        }
    
        @Override
        public Void call() throws Exception {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    LOGGER.log("received %s", line);
                    NewsItem item = newsItemProducer.apply(line);
                    if (item != null) {
                        queue.offer(item);
                    }
                }
            } catch (IOException e) {                                                      
                System.out.printf("client %s disconnected %n", socket.toString());
            } finally {
                socket.close();  
            }
            return null;
        }
    }
    
    public static void main(String[] args) {
        String port = "8913";
        if (args.length > 0) {
            port = args[0];    
        } else {
            System.out.printf("Usage: Analyzer [port:%s] %n", port);
            port = System.getProperty("port", port);                
        }
        try {
            int portNr = Integer.valueOf(port);
            Analyzer analyzer = new Analyzer(portNr);
            analyzer.start();
        } catch (NumberFormatException e) {
            System.out.printf("Invalid port number %s", port);
            System.exit(-1);
        }
    }
}
                                           