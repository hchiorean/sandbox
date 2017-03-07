package org.test.news;

import java.util.Objects;

/**
 * Simple abstraction of a news item in the context of a {@link Analyzer} and {@link Feed}
 * This class intentionally implements reverse-comparable semantics so that the highest prio news items are placed at the 
 * beginning of a sorted collection
 * 
 * @author Horia Chiorean (horia.chiorean@gmail.com)
 */
public class NewsItem implements Comparable<NewsItem> {
    
    private final String message;
    private final byte priority;
    private final String feedId;
    
    protected NewsItem(byte priority, String message, String feedId) {
        this.priority = priority;
        this.message = message;
        this.feedId = feedId;
        validate();
    }
    
    private void validate() {
        Objects.requireNonNull(message, "message cannot be null");
        if (message.trim().length() <= 1) {
            throw new IllegalArgumentException("message cannot be empty and must contain more than 1 char");
        }
        if (priority < 0 || priority > 9) {
            throw new IllegalArgumentException("priority must be between 0 and 9");
        }
        Objects.requireNonNull(feedId, "the feed cannot be null");
    }
   
    protected String headline() {
        return message + " " + priority;
    }
    
    @Override
    public int compareTo(NewsItem other) {
       if (other.priority != priority) {
           return Integer.compare(other.priority, priority);
       } else if (!other.message.equals(message)) {
           // lexicographic is fine, we just need it to be stable
           return other.message.compareTo(message);
       } else if (!other.feedId.equals(feedId)) {
           return other.feedId.compareTo(feedId);
       }
       return 0;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NewsItem newsItem = (NewsItem) o;
        return priority == newsItem.priority &&
               Objects.equals(message, newsItem.message) &&
               Objects.equals(feedId, newsItem.feedId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(message, priority, feedId);
    }
    
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("NewsItem[");
        sb.append("message='").append(message).append('\'');
        sb.append(", priority=").append(priority);
        sb.append(']');
        return sb.toString();
    }
}
