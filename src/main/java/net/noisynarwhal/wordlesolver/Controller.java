package net.noisynarwhal.wordlesolver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@RestController
public class Controller {
    private static String WORDS_FILE = "/words.txt";
    private final Set<String> wordList = new TreeSet<>();

    public Controller() {

        try {
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Controller.class.getResourceAsStream(WORDS_FILE))))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim().toLowerCase();
                    this.wordList.add(line);
                }
            }

        } catch(Throwable th) {
            throw new RuntimeException("Failed to load word list", th);
        }
    }

    @GetMapping("/")
    public String index() {
        return "Wordle Solver";
    }

    @GetMapping("/guesses")
    SortedSet<Suggestion> guesses(@RequestBody List<Guess> guesses) {
        final WordleSolver solver = new WordleSolver(this.wordList);
        for(final Guess guess : guesses) {
            solver.update(guess);
        }
        return solver.getBestSuggestions();
    }

}