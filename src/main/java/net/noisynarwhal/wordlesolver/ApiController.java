package net.noisynarwhal.wordlesolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("${api.base-path}")
public class ApiController {
    private static final String WORDS_FILE = "/words.txt";
    private static final Set<String> WORD_LIST = new TreeSet<>();
    private final VersionConfig versionConfig;

    static {
        try {
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(ApiController.class.getResourceAsStream(WORDS_FILE))))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim().toLowerCase();
                    ApiController.WORD_LIST.add(line);
                }
            }

        } catch(Throwable th) {
            throw new RuntimeException("Failed to load word list", th);
        }
    }

    @Autowired
    public ApiController(VersionConfig versionConfig) {
        this.versionConfig = versionConfig;
    }

    @GetMapping(
            path = "/",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, String> index() {
        return Map.of(
                "name", "Wordle Solver API",
                "version", this.versionConfig.getVersion(),
                "status", "operational"
        );
    }

    @PostMapping(
            path = "/solve",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> solve(@RequestBody List<Guess> guesses) {
        final WordleSolver solver = new WordleSolver(ApiController.WORD_LIST);
        for(final Guess guess : guesses) {
            solver.update(guess);
        }

        final SortedSet<Suggestion> suggestions = solver.getBestSuggestions();

        return Map.of(
                "suggestions", suggestions,
                "timestamp", System.currentTimeMillis(),
                "count", suggestions.size(),
                "apiVersion", this.versionConfig.getVersion()
        );
    }

}