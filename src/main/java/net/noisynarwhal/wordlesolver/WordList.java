package net.noisynarwhal.wordlesolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class WordList {
    private static final String WORDS_FILE = "/words.txt";
    private static final Set<String> WORD_LIST = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(WordList.class);

    static {
        try {
            try (final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(WordList.class.getResourceAsStream(WORDS_FILE))))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String word = line.trim().toUpperCase();
                    if (word.matches("^[A-Z]{5}$")) {
                        WordList.WORD_LIST.add(word);
                    } else {
                        logger.warn("Invalid word in word list: {}", word);
                    }
                }
            }
        } catch (Throwable th) {
            throw new RuntimeException("Failed to load word list", th);
        }
    }


    private WordList() {
        // Prevent instantiation
    }

    public static Set<String> getWords() {
        return Collections.unmodifiableSet(WordList.WORD_LIST);
    }

}