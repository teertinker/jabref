package net.sf.jabref.logic.search;

import net.sf.jabref.logic.search.rules.util.SentenceAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class SearchQueryHighlightObservable {

    private final ArrayList<SearchQueryHighlightListener> listeners = new ArrayList<>();

    private Optional<Pattern> pattern = Optional.empty();

    /**
     * Adds a SearchQueryHighlightListener to the search bar. The added listener is immediately informed about the current search.
     * Subscribers will be notified about searches.
     *
     * @param l SearchQueryHighlightListener to be added
     */
    public void addSearchListener(SearchQueryHighlightListener l) {
        if (listeners.contains(l)) {
            return;
        } else {
            listeners.add(l);
        }

        // fire event for the new subscriber
        l.highlightPattern(pattern);
    }

    public int getListenerCount() {
        return listeners.size();
    }

    /**
     * Remove a SearchQueryHighlightListener
     *
     * @param l SearchQueryHighlightListener to be removed
     */
    public void removeSearchListener(SearchQueryHighlightListener l) {
        listeners.remove(l);
    }

    /**
     * Parses the search query for valid words and returns a list these words. For example, "The great Vikinger" will
     * give ["The","great","Vikinger"]
     *
     * @param searchText the search query
     * @return list of words found in the search query
     */
    private List<String> getSearchwords(String searchText) {
        return (new SentenceAnalyzer(searchText)).getWords();
    }

    /**
     * Fires an event if a search was started (or cleared)
     *
     * @param searchText the search query
     */
    public void fireSearchlistenerEvent(SearchQuery searchQuery) {
        // Parse the search string to words
        if (searchQuery == null || searchQuery.isGrammarBasedSearch()) {
            pattern = Optional.empty();
        } else {
            pattern = getPatternForWords(getSearchwords(searchQuery.query), searchQuery.regularExpression, searchQuery.caseSensitive);
        }

        update();
    }

    private void update() {
        // Fire an event for every listener
        for (SearchQueryHighlightListener s : listeners) {
            s.highlightPattern(pattern);
        }
    }

    // Returns a regular expression pattern in the form (w1)|(w2)| ... wi are escaped if no regular expression search is enabled
    public static Optional<Pattern> getPatternForWords(List<String> words, boolean useRegex, boolean isCaseSensitive) {
        if ((words == null) || words.isEmpty() || words.get(0).isEmpty()) {
            return Optional.empty();
        }

        // compile the words to a regular expression in the form (w1)|(w2)|(w3)
        StringJoiner joiner = new StringJoiner(")|(", "(", ")");
        for (String word : words) {
            joiner.add(useRegex ? word : Pattern.quote(word));
        }
        String searchPattern = joiner.toString();

        if (isCaseSensitive) {
            return Optional.of(Pattern.compile(searchPattern));
        } else {
            return Optional.of(Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE));
        }
    }

}
