package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.model.document.Question;
import by.gsu.duelingobackend.model.enums.QuestionType;
import by.gsu.duelingobackend.model.enums.QuestionDifficulty;
import by.gsu.duelingobackend.repository.question.QuestionRepository;
import by.gsu.duelingobackend.repository.test.TestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentBootstrapServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private CacheManager cacheManager;

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void loadsBundledQuestionsAndBuildsTestsWhenMongoIsEmpty() {
        ContentBootstrapService service = new ContentBootstrapService(
                questionRepository,
                testRepository,
                new ObjectMapper(),
                cacheManager
        );
        ReflectionTestUtils.setField(
                service,
                "questionsResource",
                new ClassPathResource("data/questions.json")
        );

        when(questionRepository.findAll()).thenReturn(List.of());
        when(questionRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<Question> questions = invocation.getArgument(0);
            return StreamSupport.stream(questions.spliterator(), false).toList();
        });
        when(testRepository.findAll()).thenReturn(List.of());
        when(testRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.run(null);

        ArgumentCaptor<Iterable<Question>> questionCaptor = ArgumentCaptor.forClass(Iterable.class);
        ArgumentCaptor<Iterable<by.gsu.duelingobackend.model.document.Test>> testCaptor =
                ArgumentCaptor.forClass(Iterable.class);
        verify(questionRepository).saveAll(questionCaptor.capture());
        verify(testRepository).saveAll(testCaptor.capture());

        List<Question> questions = StreamSupport.stream(
                questionCaptor.getValue().spliterator(),
                false
        ).toList();
        List<by.gsu.duelingobackend.model.document.Test> tests = StreamSupport.stream(
                testCaptor.getValue().spliterator(),
                false
        ).toList();

        assertThat(questions).hasSize(480);
        assertThat(tests).hasSize(42);
        assertThat(questions)
                .filteredOn(question -> question.getType() == QuestionType.AUDIO_RECOGNITION)
                .hasSize(60)
                .allSatisfy(question -> {
                    assertThat(question.getTopic()).isEqualTo("Listening");
                    assertThat(question.getOptions()).containsExactly(question.getQuestionText());
                    assertThat(question.getCorrectAnswers()).containsExactly(question.getQuestionText());
                });
        assertThat(questions)
                .filteredOn(question -> question.getType() == QuestionType.SENTENCE_CONSTRUCTION)
                .hasSize(126)
                .allSatisfy(question -> {
                    assertThat(question.getQuestionText()).containsPattern("[А-Яа-яЁё]");
                    assertThat(question.getCorrectAnswers()).hasSize(1);
                    List<String> answerWords = List.of(question.getCorrectAnswers().get(0).split("\\s+"))
                            .stream()
                            .map(word -> word.replaceAll("[,.!?;:]", ""))
                            .toList();
                    List<String> optionWords = question.getOptions().stream()
                            .map(word -> word.replaceAll("[,.!?;:]", ""))
                            .toList();
                    assertThat(optionWords).containsExactlyInAnyOrderElementsOf(answerWords);
                });
        assertThat(questions)
                .filteredOn(question -> question.getType() == QuestionType.FILL_IN_INPUT)
                .hasSize(126)
                .allSatisfy(question -> {
                    assertThat(question.getQuestionText()).contains("(подсказка:");
                    assertThat(question.getOptions()).isEmpty();
                    assertThat(question.getCorrectAnswers()).isNotEmpty();
                });
        assertThat(questions)
                .filteredOn(question -> question.getType() == QuestionType.FILL_IN_CHOICE)
                .allSatisfy(question -> {
                    assertThat(question.getOptions()).hasSizeGreaterThanOrEqualTo(3);
                    assertThat(question.getOptions()).containsAnyElementsOf(question.getCorrectAnswers());
                });
        assertThat(questions)
                .extracting(Question::getQuestionText)
                .doesNotHaveDuplicates();
        assertThat(tests).allSatisfy(test -> assertThat(test.getQuestions()).hasSize(10));
        assertThat(tests.stream()
                .map(by.gsu.duelingobackend.model.document.Test::getTopic)
                .distinct()
                .toList()).hasSize(14);
        assertThat(tests).filteredOn(test -> test.getDifficulty() == QuestionDifficulty.EASY).hasSize(14);
        assertThat(tests).filteredOn(test -> test.getDifficulty() == QuestionDifficulty.MEDIUM).hasSize(14);
        assertThat(tests).filteredOn(test -> test.getDifficulty() == QuestionDifficulty.HARD).hasSize(14);
    }
}
