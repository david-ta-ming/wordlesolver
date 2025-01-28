package net.noisynarwhal.wordlesolver;

import java.util.Objects;

/**
 * A suggestion for a Wordle guess.
 */
public record Suggestion(String word, double entropy, boolean isPossibleAnswer) implements Comparable<Suggestion> {

    @Override
    public int compareTo(Suggestion other) {
        final int entropyCompare = Double.compare(other.entropy, this.entropy);
        if (entropyCompare != 0) {
            return entropyCompare;
        } else if (this.isPossibleAnswer != other.isPossibleAnswer) {
            return Boolean.compare(other.isPossibleAnswer, this.isPossibleAnswer);
        } else {
            return this.word.compareTo(other.word);
        }
    }

    @Override
    public String toString() {
        return this.word + " (" + this.entropy + ',' + this.isPossibleAnswer + ')';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Suggestion guess1 = (Suggestion) other;
        return Objects.equals(this.word, guess1.word);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.word);
    }
}
