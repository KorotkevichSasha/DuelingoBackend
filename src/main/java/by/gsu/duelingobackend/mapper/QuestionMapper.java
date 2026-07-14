package by.gsu.duelingobackend.mapper;

import by.gsu.duelingobackend.dto.response.AudioQuestionResponse;
import by.gsu.duelingobackend.dto.response.QuestionDetailedResponse;
import by.gsu.duelingobackend.model.document.Question;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface QuestionMapper {

    QuestionDetailedResponse toDetailedResponse(Question question);
    AudioQuestionResponse toAudioResponse(Question question);
}
