package net.noisynarwhal.wordlesolver;

import java.util.*;

/**
 * A Wordle solver that suggests the best guesses based on the feedback received.
 * The operations in this class are synchronized to ensure thread safety.
 */
public class WordleSolver {
    public static final int MIN_SUGGESTIONS = 5;
    private final Set<String> possibleWords = new HashSet<>();
    private final Set<String> allWords = new HashSet<>();

    /**
     * Create a new WordleSolver with the given word list.
     *
     * @param wordList the list of words to use
     */
    public WordleSolver(Iterable<String> wordList) {
        for (final String s : wordList) {
            final String word = s.trim().toUpperCase();
            if (word.length() == 5) {
                this.possibleWords.add(word);
                this.allWords.add(word);
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
     * Get the best suggestions based on the current state of the solver.
     * Returns the top MIN_SUGGESTIONS suggestions and any additional suggestions that are possible answers.
     *
     * @return a sorted set of suggestions
     */
    public synchronized SortedSet<Suggestion> getBestSuggestions() {

        final SortedSet<Suggestion> bestSuggestions = new TreeSet<>();

        if (!this.possibleWords.isEmpty()) {

            // If only one word remains, return it
            if (this.possibleWords.size() == 1) {

                final String word = this.possibleWords.iterator().next();
                final double entropy = calculateEntropy(word);
                bestSuggestions.add(new Suggestion(word, entropy, true));

            } else {

                // Consider all valid words as potential guesses, not just possible answers
                for (final String word : this.allWords) {

                    final double entropy = calculateEntropy(word);
                    final boolean isPossibleAnswer = this.possibleWords.contains(word);

                    bestSuggestions.add(new Suggestion(word, entropy, isPossibleAnswer));
                }

                final Iterator<Suggestion> iterator = bestSuggestions.iterator();
                int i = 0;
                while (iterator.hasNext() && i++ < WordleSolver.MIN_SUGGESTIONS) {
                    iterator.next();
                }
                while (iterator.hasNext()) {
                    final Suggestion suggestion = iterator.next();
                    if (!suggestion.isPossibleAnswer()) {
                        iterator.remove();
                    }
                }
            }
        }

        return bestSuggestions;
    }

    /**
     * Calculate the entropy of guessing 'guess' based on the current state of the solver.
     *
     * @param guess the word to guess
     * @return the entropy of guessing 'guess'
     */
    private double calculateEntropy(String guess) {
        final Map<String, Integer> patternCounts = new HashMap<>();
        final int totalWords = this.possibleWords.size();

        // Count how many words would match each possible feedback pattern
        for (final String possibleAnswer : this.possibleWords) {
            final String pattern = generatePattern(guess, possibleAnswer);
            patternCounts.merge(pattern, 1, Integer::sum);
        }

        // Calculate entropy using the pattern probabilities
        double entropy = 0.0;
        for (int count : patternCounts.values()) {
            final double probability = (double) count / totalWords;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        return Math.round(entropy * 100) / 100.0;
    }

}
