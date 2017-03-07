package org.test.news;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Scores a series of space-separated words, based on the number of positive and negative words.
 * 
 * @author Horia Chiorean (horia.chiorean@gmail.com)
 * @see Dictionary
 */
public class PhraseScorer {
    
    protected static final PhraseScorer INSTANCE = new PhraseScorer();
    
    private static final Map<String, Integer> SCORE_MAP;
    
    static {
        SCORE_MAP = new HashMap<>(Dictionary.INSTANCE.allWords().size());
        Dictionary.INSTANCE.goodWords().forEach(word -> SCORE_MAP.put(word, 1));
        Dictionary.INSTANCE.badWords().forEach(word -> SCORE_MAP.put(word, -1));
    }
    
    private PhraseScorer() {
    }
    
    protected int scoreMessage(String message) {
        Objects.requireNonNull(message);
        return Arrays.stream(message.split(" "))
                     .mapToInt(word -> SCORE_MAP.getOrDefault(word, 0))
                     .sum();
    }
    
    public static void main(String[] args) {
        //junit...sigh
        if (1 != INSTANCE.scoreMessage("up rise down")) {
            throw new IllegalStateException("incorrect");
        } else if (3 != INSTANCE.scoreMessage("up up up")) {
            throw new IllegalStateException("incorrect");
        } else if (0 != INSTANCE.scoreMessage("???")) {
            throw new IllegalStateException("incorrect");
        } else if (-1 != INSTANCE.scoreMessage("über fall bad")) {
            throw new IllegalStateException("incorrect");
        }
    }
}
