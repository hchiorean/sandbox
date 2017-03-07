package org.test.news;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple holder of words in the context of {@link Analyzer} and {@link Feed}
 * 
 * @author Horia Chiorean (horia.chiorean@gmail.com)
 */
public final class Dictionary {
    
    protected static final Dictionary INSTANCE = new Dictionary();
    
    private static final List<String> GOOD_WORDS = Collections.unmodifiableList(
            Arrays.asList("up", "rise", "good", "success", "high", "über"));
    
    private static final List<String> BAD_WORDS = Collections.unmodifiableList(
            Arrays.asList("down", "fall", "bad", "failure", "low", "unter"));
    
    private static final List<String> ALL_WORDS = Collections.unmodifiableList(
            Stream.concat(GOOD_WORDS.stream(), BAD_WORDS.stream())
                  .collect(Collectors.toList()));
    
    private Dictionary() {
    }
    
    protected List<String> allWords() {
        return ALL_WORDS;
    }
    
    protected List<String> goodWords() {
        return GOOD_WORDS;
    }                     
    
    protected List<String> badWords() {
        return BAD_WORDS;
    }
}
