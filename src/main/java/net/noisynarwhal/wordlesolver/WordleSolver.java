package net.noisynarwhal.wordlesolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * A Wordle solver that suggests the best guesses based on the feedback received.
 * The operations in this class are synchronized to ensure thread safety.
 */
public class WordleSolver {
    public static final int MIN_SUGGESTIONS = 5;
    private static final double LOG2 = Math.log(2);
    private final Set<String> possibleWords = new HashSet<>();
    private final Set<String> allWords = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(WordleSolver.class);

    /**
     * Create a new WordleSolver with the default word list.
     */
    public WordleSolver() {
        this.possibleWords.addAll(WordList.getWords());
        this.allWords.addAll(WordList.getWords());
    }

    /**
     * Create a new WordleSolver with the given word list.
     *
     * @param wordList the list of words to use
     */
    public WordleSolver(Iterable<String> wordList) {
        for (final String s : wordList) {
            final String word = s.trim().toUpperCase();
            if (word.matches("^[A-Z]{5}$")) {
                this.possibleWords.add(word);
                this.allWords.add(word);
            } else {
                logger.warn("Invalid word in word list: {}", word);
            }
        }

    }

    /**
     * Generate the feedback pattern that would result from guessing 'guess' when 'target' is the target. The feedback
     * pattern is a string of 5 characters, where 'G' indicates a correct letter in the correct position, 'Y' indicates
     * a correct letter in the wrong position, and 'B' indicates an incorrect letter.
     *
     * @param guess  the word to guess
     * @param target the target word
     * @return the feedback pattern
     */
    private static String generatePattern(String guess, String target) {
        guess = guess.toUpperCase();
        target = target.toUpperCase();

        final char[] pattern = new char[5];
        final boolean[] usedInAnswer = new boolean[5];
        final boolean[] usedInGuess = new boolean[5];

        // First pass: mark green letters
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == target.charAt(i)) {
                pattern[i] = 'G';
                usedInAnswer[i] = true;
                usedInGuess[i] = true;
            }
        }

        // Second pass: mark yellow letters
        for (int i = 0; i < 5; i++) {
            if (!usedInGuess[i]) {
                final char guessChar = guess.charAt(i);
                boolean found = false;

                for (int j = 0; j < 5; j++) {
                    if (!usedInAnswer[j] && target.charAt(j) == guessChar) {
                        pattern[i] = 'Y';
                        usedInAnswer[j] = true;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    pattern[i] = 'B';
                }
            }
        }

        return new String(pattern);
    }

    /**
     * Check if 'word' is a possible answer given the feedback 'guess'
     *
     * @param word  the word to check
     * @param guess the feedback
     * @return true if 'word' is a possible answer, false otherwise
     */
    private static boolean isWordPossible(String word, Guess guess) {
        return WordleSolver.generatePattern(guess.word(), word).equals(guess.feedback());
    }

    /**
     * Update the solver with the feedback from a guess.
     *
     * @param guess the feedback from the guess
     */
    public synchronized void update(Guess guess) {
        this.possibleWords.removeIf(word -> !WordleSolver.isWordPossible(word, guess));
    }

    /**
     * Generate the best suggestions for the next Wordle guess based on the current state of possible words.
     * The method considers both entropy and whether a word is a possible answer to prioritize suggestions.
     * <p>
     * The process follows these steps:
     * 1. If no possible words remain, an empty set is returned.
     * 2. For each word in the complete word list (`allWords`), the entropy is calculated to determine
     *    how informative the guess would be. Words that are still valid possible answers are marked as such.
     * 3. The suggestions are sorted by entropy (high to low) and added to the result set.
     * 4. If the top suggestion (i.e., the word with the highest entropy) is a possible answer,
     *    non-answer words are removed from the suggestions.
     * 5. If the top suggestion is not a possible answer, at least `MIN_SUGGESTIONS` are retained,
     *    and additional non-answer words are filtered out.
     * <p>
     * This approach helps balance between finding an optimal guess and providing enough variety in suggestions.
     *
     * @return a sorted set of suggestions, ordered by descending entropy
     */
    public synchronized SortedSet<Suggestion> getBestSuggestions() {
        // Return an empty set if no possible words remain
        if (this.possibleWords.isEmpty()) {
            return Collections.emptySortedSet();
        }

        // Create a sorted set to store suggestions
        final SortedSet<Suggestion> bestSuggestions = new TreeSet<>();

        // Generate suggestions by calculating entropy for all valid words
        for (final String word : this.allWords) {
            final double entropy = calculateEntropy(word);
            final boolean isPossibleAnswer = this.possibleWords.contains(word);
            bestSuggestions.add(new Suggestion(word, entropy, isPossibleAnswer));
        }

        // Check the top suggestion to determine filtering behavior
        final Suggestion firstSuggestion = bestSuggestions.first();

        if (firstSuggestion.isPossibleAnswer()) {
            // If the top suggestion is a possible answer, retain only suggestions that are possible answers
            bestSuggestions.removeIf(suggestion -> !suggestion.isPossibleAnswer());
        } else {
            // If the top suggestion is not a possible answer:
            // 1. Create a temporary set to store our selections
            SortedSet<Suggestion> selectedSuggestions = new TreeSet<>();

            // 2. Add the top MIN_SUGGESTIONS for information gain
            bestSuggestions.stream()
                    .limit(MIN_SUGGESTIONS)
                    .forEach(selectedSuggestions::add);

            // 3. Add all possible answers for completeness
            bestSuggestions.stream()
                    .filter(Suggestion::isPossibleAnswer)
                    .forEach(selectedSuggestions::add);

            // 4. Replace the original set with our selection
            bestSuggestions.clear();
            bestSuggestions.addAll(selectedSuggestions);
        }

        // Return the sorted set of best suggestions
        return bestSuggestions;
    }



    /**
     * Calculate the entropy of guessing 'guess' based on the current state of the solver.
     *
     * @param guess the word to guess
     * @return the entropy of guessing 'guess'
     */
    private double calculateEntropy(String guess) {
        // Handle edge case where no possible words remain
        if (this.possibleWords.isEmpty()) {
            return 0.0;
        }

        final Map<String, Integer> patternCounts = new HashMap<>();
        final int totalWords = this.possibleWords.size();

        // Count how many words would match each possible feedback pattern
        for (final String possibleAnswer : this.possibleWords) {
            final String pattern = generatePattern(guess, possibleAnswer);
            patternCounts.merge(pattern, 1, Integer::sum);
        }

        // Calculate entropy using Math.log2 for better performance and clarity
        double entropy = 0.0;
        for (final int count : patternCounts.values()) {
            final double probability = (double) count / totalWords;
            entropy -= probability * (Math.log(probability) / WordleSolver.LOG2);
        }

        // Round to 2 decimal places
        return Math.round(entropy * 100) / 100.0;
    }

}
