# Wordle Solver

A Spring Boot application that helps solve Wordle puzzles using information theory and entropy calculations. This solver recommends optimal guesses based on the maximum information gain principle.

## Features

- RESTful API for getting word suggestions
- Web interface with interactive game board
- Entropy-based word ranking system
- Support for multiple concurrent games
- Real-time feedback and suggestions

## How It Works

This Wordle solver uses information theory to make intelligent guesses. Information theory, developed by Claude Shannon in the 1940s, gives us powerful tools to measure and reason about uncertainty and information. At the heart of our solver is the concept of entropy, which helps us measure how much information we gain from each guess.

### Understanding Information in Wordle

When you play Wordle, each guess gives you information through the colored squares:
- A green square (🟩) tells you exactly what letter goes in that position
- A yellow square (🟨) tells you a letter that appears somewhere else
- A gray square (⬛️) tells you a letter that isn't in the word

Think about your first guess in Wordle. Before you guess, any five-letter word could be the answer. After you guess, the colored squares narrow down the possibilities. The more possibilities your guess can eliminate, the more information it gives you.

### The Mathematics of Information

Information theory tells us that the amount of information we receive is related to how surprised we are by what we learn. If someone tells you something you were almost certain was true, you gain very little information. But if they tell you something unexpected, you gain more information.

In mathematical terms, if an event has probability P of occurring, the information we gain from observing that event is -log₂(P) bits. We use log base 2 because information is traditionally measured in bits, where one bit can distinguish between two equally likely possibilities.

Let's understand this with a simple coin flip example. Imagine you have a friend who flipped a coin and knows the result. How much information do you gain when they tell you the outcome?

With a fair coin, heads and tails are equally likely, each with probability 1/2. When your friend tells you the result, you gain -log₂(1/2) = 1 bit of information. This matches our intuition perfectly: one bit is exactly what a computer needs to distinguish between two possibilities (like 0 and 1, or heads and tails).

Now imagine the coin is unfair - it lands on heads 75% of the time. If your friend tells you they got heads, you gain -log₂(3/4) ≈ 0.415 bits of information. But if they tell you they got tails, you gain -log₂(1/4) = 2 bits! This also makes intuitive sense: learning about a rare outcome (tails) tells you more than learning about a common outcome (heads), because the rare outcome is more surprising.

This connects directly to Wordle: some color patterns are more common than others, just like how heads was more common with our unfair coin. Rare patterns give us more information, which is exactly what our entropy calculation measures.

### Entropy Formula

Entropy extends this idea to situations where we might see different outcomes, each with their own probabilities. The entropy formula is:

H = -∑ P(x) × log₂(P(x))

Where:
- H is the entropy (expected information gain)
- P(x) is the probability of outcome x
- The sum is taken over all possible outcomes
- log₂ is the logarithm with base 2

This formula gives us the average amount of information we expect to gain. Higher entropy means we expect to gain more information from making that guess.

### Measuring Information with Entropy in Wordle

Let's see how this applies to Wordle. When we make a guess, we'll see a pattern of colored squares. Some patterns are more common than others, and each pattern eliminates different possible answers.

For example, let's guess "STARE" and look at what patterns we might see:
- Against "PAINT", you'd see ⬛️🟩🟩⬛️⬛️ (BGGRB) - matching T and A positions
- Against "STAKE", you'd see 🟩🟩🟩⬛️🟩 (GGGBG) - matching all but R
- Against "PAUSE", you'd see ⬛️🟨⬛️⬛️🟩 (BYBGE) - A appears elsewhere, E matches

Let's work through a concrete example with a small set of words. Imagine we have just 10 possible answers left, and when we guess "CRANE":
- Pattern BBYYB appears 5 times (probability = 5/10 = 0.5)
- Pattern GBBBG appears 3 times (probability = 3/10 = 0.3)
- Pattern GGGBB appears 2 times (probability = 2/10 = 0.2)

For each pattern, we calculate P × log₂(P):
- For BBYYB: 0.5 × log₂(0.5) ≈ -0.500
- For GBBBG: 0.3 × log₂(0.3) ≈ -0.521
- For GGGBB: 0.2 × log₂(0.2) ≈ -0.464

Adding these up and negating gives us our entropy:
-(-0.500 - 0.521 - 0.464) = 1.485 bits

What does this number mean? Each bit of entropy represents the power to cut our possibilities in half. With 1.485 bits of entropy, this guess will typically eliminate about 64% of the remaining possibilities (1 - 1/2¹·⁴⁸⁵). A higher entropy value would mean we expect to eliminate even more possibilities.

### Implementation Details

The solver calculates entropy for every possible guess and chooses the one with the highest value. Here's how the core calculation works in our code:

```java
private double calculateEntropy(String guess) {
    // Skip if no possible words remain
    if (this.possibleWords.isEmpty()) {
        return 0.0;
    }

    // Count how often each pattern appears
    final Map<String, Integer> patternCounts = new HashMap<>();
    final int totalWords = this.possibleWords.size();

    for (final String possibleAnswer : this.possibleWords) {
        // Generate the pattern we'd see if this was the answer
        final String pattern = generatePattern(guess, possibleAnswer);
        // Count how many times we see each pattern
        patternCounts.merge(pattern, 1, Integer::sum);
    }

    // Calculate entropy using the formula H = -∑ P(x) × log₂(P(x))
    double entropy = 0.0;
    for (int count : patternCounts.values()) {
        final double probability = (double) count / totalWords;
        entropy -= probability * (Math.log(probability) / LOG2);
    }

    // Round to 2 decimal places for cleaner output
    return Math.round(entropy * 100) / 100.0;
}
```

This approach is particularly effective because:
- It considers all possible answers and patterns
- It weighs the information value of each possible outcome
- It helps us eliminate as many possibilities as possible with each guess
- It adapts as we learn more information from each guess

The solver doesn't just look for high-entropy guesses—it also considers whether a word could be the actual answer. This creates a balance between gathering information and trying to win the game, which is why you'll sometimes see the solver suggest a possible answer even if it's not the highest-entropy guess.

### Trade-offs and Strategy

While entropy gives us a powerful way to measure the information value of a guess, it's not the only factor to consider. The solver balances several competing goals:

1. Information Gain: Choosing words with high entropy to learn as much as possible.
2. Solution Finding: Preferring words that could be the actual answer.
3. Worst-Case Performance: Considering how bad our position would be if we get unlucky.

This is why you might sometimes see the solver make what seems like a suboptimal guess. It's not just maximizing entropy—it's trying to win the game efficiently while avoiding particularly bad outcomes.

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