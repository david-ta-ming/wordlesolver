package net.noisynarwhal.wordlesolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("${api.base-path}")
public class ApiController {
    private final VersionConfig versionConfig;

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

        final WordleSolver solver = new WordleSolver();

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