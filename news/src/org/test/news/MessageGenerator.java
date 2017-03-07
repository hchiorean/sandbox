package org.test.news;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Generates random messages with a format of "headline priority", where headline is 3-5 words and priority a number between 0-9
 * based on some probability rules
 * 
 * @author Horia Chiorean (horia.chiorean@gmail.com)
 */
public class MessageGenerator {
    
    private static final byte[] PRIORITIES = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final double[] PROBABILITIES = new double[] {29.3, 19.3, 14.3, 10.9, 8.4, 6.5, 4.8, 3.4, 2.1, 1};
   
    private static final byte[] PRIORITIES_SOURCE;    
    
    static {
        // fill the array which will be used to generate randoms based on probabilities
        int[] PROBABILISTIC_ARRAY = DoubleStream.of(PROBABILITIES)
                                                .mapToInt(p -> (int) (p * 10))
                                                .toArray();
        int size = IntStream.of(PROBABILISTIC_ARRAY).sum();
        PRIORITIES_SOURCE = new byte[size];
        // fill each position in the array with the element from PRIORITIES multiplies by the number of times from PROBABILISTIC_ARRAY
        for (int i = 0; i < PRIORITIES.length; i++) {
            int priority = PRIORITIES[i];
            int numberOfTimes = PROBABILISTIC_ARRAY[i];
            for (int j = 0; j < numberOfTimes; j++) {
                PRIORITIES_SOURCE[j] = (byte) priority;
            }
        }
    }
    
    private final int minWords;
    private final int maxWords;
    
    protected MessageGenerator(int minWords, int maxWords) {
        this.minWords = minWords;
        this.maxWords = maxWords;
    }
    
    /**
     * Generates a new random message with a [headline] [priority] format
     * 
     * @return a {@link String}, never {@code null}
     */
    public String generateMessage() {
        List<String> allWords = Dictionary.INSTANCE.allWords();
        ThreadLocalRandom tlRandom = ThreadLocalRandom.current();
        int howMany = tlRandom.nextInt(minWords, maxWords + 1);
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < howMany; i++) {
            int wordIdx = tlRandom.nextInt(allWords.size());
            message.append(allWords.get(wordIdx)).append(" ");
        }
        byte priority = PRIORITIES_SOURCE[tlRandom.nextInt(PRIORITIES_SOURCE.length)];
        message.append(priority);
        return message.toString();
    }
    
    /**
     * Generates a variable number of messages giving them up for consumption to a {@link Consumer}
     * @param count the number of message to generate; must be positive
     * @param consumer the consumer of these messages; never null
     */
    public void generateMessages(int count, Consumer<String> consumer) {
        Objects.requireNonNull(consumer);
        for (int i = 0; i < count; i++) {
            consumer.accept(generateMessage());
        }
    }
    
    public static void main(String[] args) {
        //junit...sigh
        MessageGenerator messageGenerator = new MessageGenerator(3, 5);
        for (int i = 0; i < 1000; i++) {
            String message = messageGenerator.generateMessage();
            System.out.println(message);
            String[] elements = message.split(" ");
            if (elements.length < 4 || elements.length > 6) {
                throw new IllegalStateException("Invalid message");
            }
            Integer.valueOf(elements[elements.length - 1]);
        }
    }
}
