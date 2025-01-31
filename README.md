# Wordle Solver

A Spring Boot application that helps solve Wordle puzzles using information theory and entropy calculations. This solver recommends optimal guesses based on the maximum information gain principle.

## Features

- RESTful API for getting word suggestions
- Web interface with interactive game board
- Entropy-based word ranking system
- Support for multiple concurrent games
- Real-time feedback and suggestions

## How It Works

### Information Theory and Entropy in Wordle

The solver uses information theory to determine the best possible guesses. The core concept is entropy, which measures the average amount of information gained from making a particular guess.

#### Understanding Entropy in This Context

In Wordle, entropy represents how much a guess will reduce our uncertainty about the target word. The higher the entropy, the more information we expect to gain from that guess.

##### Entropy Formula

For a given guess word, we calculate its entropy using:

H(guess) = -∑ P(pattern) * log₂(P(pattern))

Where:
- H(guess) is the entropy of the guess word
- P(pattern) is the probability of getting a specific feedback pattern
- The sum is taken over all possible feedback patterns

#### Example Calculation

Let's say we're considering the word "STARE" as our guess, and there are 2,315 possible answers remaining:

1. First, we simulate what pattern we would get for each possible answer:
   ```
   STARE vs PAINT -> 🟨⬛️🟨⬛️🟨 (pattern: "YBYBY")
   STARE vs STAKE -> 🟩🟩🟩⬛️🟩 (pattern: "GGGBG")
   ... and so on for all possible answers
   ```

2. We count how many times each pattern occurs and calculate probabilities:
   ```
   Pattern "YBYBY": 42 occurrences → P = 42/2315 ≈ 0.018
   Pattern "GGGBG": 3 occurrences → P = 3/2315 ≈ 0.001
   ... and so on
   ```

3. For each pattern, we calculate P * log₂(P):
   ```
   For "YBYBY": 0.018 * log₂(0.018) ≈ -0.098
   For "GGGBG": 0.001 * log₂(0.001) ≈ -0.011
   ```

4. Sum all these values and negate to get the final entropy:
   ```
   H("STARE") = -(sum of all P * log₂(P)) ≈ 5.87 bits
   ```

#### Why This Works

- A high entropy value means the guess tends to split possible answers into many roughly equal-sized groups
- Each bit of entropy theoretically halves the remaining possibilities
- The optimal first guess maximizes this entropy value

### Implementation Details

The entropy calculation is implemented in `WordleSolver.java`:

```java
private double calculateEntropy(String guess) {
    if (this.possibleWords.isEmpty()) {
        return 0.0;
    }

    final Map<String, Integer> patternCounts = new HashMap<>();
    final int totalWords = this.possibleWords.size();

    // Count pattern frequencies
    for (final String possibleAnswer : this.possibleWords) {
        final String pattern = generatePattern(guess, possibleAnswer);
        patternCounts.merge(pattern, 1, Integer::sum);
    }

    // Calculate entropy using the formula H = -∑ P(x) * log₂(P(x))
    double entropy = 0.0;
    for (int count : patternCounts.values()) {
        final double probability = (double) count / totalWords;
        entropy -= probability * (Math.log(probability) / Math.log(2));
    }

    return Math.round(entropy * 100) / 100.0;
}
```

### Pattern Generation

The pattern generation is a crucial part of the entropy calculation. For each guess-target pair, we generate a pattern following Wordle's rules:

1. First pass marks correct letters in correct positions (🟩)
2. Second pass marks correct letters in wrong positions (🟨)
3. Remaining letters are marked as incorrect (⬛️)

The implementation handles duplicate letters according to Wordle's rules, where:
- Each target letter can only be matched once
- Green matches take priority over yellow matches

## API Usage

### Solve Endpoint

```bash
POST /api/v1/solve
Content-Type: application/json

[
  {
    "word": "STARE",
    "feedback": "GYBBG"
  }
]
```

Response:
```json
{
  "suggestions": [
    {
      "word": "SHINE",
      "entropy": 4.92,
      "isPossibleAnswer": true
    },
    ...
  ],
  "count": 10,
  "timestamp": 1643673600000,
  "apiVersion": "1.0"
}
```

## Building and Running

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Build Commands

```bash
# Build the project
mvn clean package

# Run tests
mvn test

# Run the application
java -jar target/wordlesolver.jar
```

### Running with Docker

```bash
# Build the Docker image
docker build -t wordlesolver .

# Run the container
docker run -p 8080:8080 wordlesolver
```

## Development Guidelines

### Adding New Features

1. Write tests first (TDD approach)
2. Document any new endpoints or parameters
3. Update entropy calculations if word list changes

### Testing

The project includes:
- Unit tests for core logic
- Integration tests for API endpoints
- Performance tests for entropy calculations

## Contributing

1. Fork the repository
2. Create a feature branch
3. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Based on information theory concepts from Claude Shannon
- Inspired by various Wordle solver implementations
- Uses Spring Boot framework for robust API development