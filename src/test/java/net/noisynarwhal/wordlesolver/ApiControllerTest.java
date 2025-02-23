package net.noisynarwhal.wordlesolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiController.class)
@Import(VersionConfig.class)
class ApiControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(ApiControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testIndex() throws Exception {
        mockMvc.perform(get("/api/v1/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Wordle Solver API"))
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.status").value("operational"));
    }

    @Test
    void testSolveWithValidGuess() throws Exception {
        List<Guess> guesses = List.of(
                new Guess("STARE", "BYGBB")
        );

        mockMvc.perform(post("/api/v1/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guesses)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.suggestions").isArray())
                .andExpect(jsonPath("$.count").isNumber())
                .andExpect(jsonPath("$.timestamp").isNumber());
    }

    @Test
    void testSolveWithInvalidGuess() throws Exception {
        String invalidJson = "[{\"word\":\"XX\",\"feedback\":\"BBB\"}]";

        mockMvc.perform(post("/api/v1/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSolveWithEmptyGuesses() throws Exception {
        mockMvc.perform(post("/api/v1/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions").isArray());
    }

    @Test
    void testSolveWithMultipleGuesses() throws Exception {
        logger.info("Testing multiple guesses");

        {
            logger.info("Guesses: [TARES, BIPOD, FLEER, ALWAY]");
            final List<Guess> guesses = Arrays.asList(
                    new Guess("TARES", "BBYGB"),
                    new Guess("BIPOD", "BBBBB"),
                    new Guess("FLEER", "GBYGG"),
                    new Guess("ALWAY", "BBBBB")
            );

            final MockHttpServletResponse response = mockMvc.perform(post("/api/v1/solve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(guesses)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.suggestions").isArray())
                    .andExpect(jsonPath("$.suggestions[0].word").value("FEVER"))
                    .andExpect(jsonPath("$.suggestions[0].isPossibleAnswer").value(true))
                    .andExpect(jsonPath("$.suggestions[0].entropy").value(0.00))
                    .andReturn()
                    .getResponse();

            final String responseContent = response.getContentAsString();
            logger.info("Response content: {}", responseContent);
        }
        {
            logger.info("Guesses: [TARES, SULPH]");
            final List<Guess> guesses = Arrays.asList(
                    new Guess("TARES", "BGBYY"),
                    new Guess("SULPH", "YBGBB")
            );

            final MockHttpServletResponse response = mockMvc.perform(post("/api/v1/solve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(guesses)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.suggestions").isArray())
                    .andExpect(jsonPath("$.suggestions.length()").value(2))
                    .andExpect(jsonPath("$.suggestions[0].word").value("FALSE"))
                    .andExpect(jsonPath("$.suggestions[0].entropy").value(1.00))
                    .andExpect(jsonPath("$.suggestions[1].word").value("VALSE"))
                    .andExpect(jsonPath("$.suggestions[1].entropy").value(1.00))
                    .andReturn()
                    .getResponse();

            final String responseContent = response.getContentAsString();
            logger.info("Response content: {}", responseContent);
        }
        {
            logger.info("Guesses: [TARES, COLIN, HUMPY]");
            final List<Guess> guesses = Arrays.asList(
                    new Guess("TARES", "YBBBB"),
                    new Guess("COLIN", "BGBBB"),
                    new Guess("HUMPY", "BBYBB")
            );

            final MockHttpServletResponse response = mockMvc.perform(post("/api/v1/solve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(guesses)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.suggestions").isArray())
                    .andExpect(jsonPath("$.suggestions.length()").value(1))
                    .andExpect(jsonPath("$.suggestions[0].word").value("MOTTO"))
                    .andExpect(jsonPath("$.suggestions[0].entropy").value(0.00))
                    .andReturn()
                    .getResponse();

            final String responseContent = response.getContentAsString();
            logger.info("Response content: {}", responseContent);
        }
        {
            logger.info("Guesses: [TARES, KNELT]");
            final List<Guess> guesses = Arrays.asList(
                    new Guess("TARES", "YBBGY"),
                    new Guess("KNELT", "BBGYY")
            );

            final MockHttpServletResponse response = mockMvc.perform(post("/api/v1/solve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(guesses)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.suggestions").isArray())
                    .andExpect(jsonPath("$.suggestions.length()").value(1))
                    .andExpect(jsonPath("$.suggestions[0].word").value("STEEL"))
                    .andExpect(jsonPath("$.suggestions[0].entropy").value(0.00))
                    .andReturn()
                    .getResponse();

            final String responseContent = response.getContentAsString();
            logger.info("Response content: {}", responseContent);
        }
    }
}