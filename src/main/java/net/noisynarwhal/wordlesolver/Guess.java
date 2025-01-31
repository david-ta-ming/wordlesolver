package net.noisynarwhal.wordlesolver;

/**
 * Represents a guess submitted by the user to Wordle. It contains the word guessed and the feedback received.
 */
public record Guess(String word, String feedback) {
    /**
     * Create a new Guess with the given word and feedback.
     *
     * @param word     the word guessed; must be 5 letters long
     * @param feedback the feedback received; must be 5 characters long composed of 'B', 'Y', 'G'
     */
    public Guess(String word, String feedback) {

        this.word = word.trim().toUpperCase();
        this.feedback = feedback.trim().toUpperCase();

        if (!this.word.matches("^[A-Z]{5}$")) {
            throw new IllegalArgumentException("Word must be 5 letters long: '" + this.word + '\'');
        }
        if (!this.feedback.matches("^[BYG]{5}$")) {
            throw new IllegalArgumentException("Feedback must be 5 characters long composed of 'B', 'Y', 'G': '" + this.feedback + '\'');
        }
    }

    @Override
    public String toString() {
        return "Guess{" +
                "word='" + word + '\'' +
                ", feedback='" + feedback + '\'' +
                '}';
    }
}
