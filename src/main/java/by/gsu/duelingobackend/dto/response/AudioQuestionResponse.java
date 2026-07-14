package by.gsu.duelingobackend.dto.response;

import by.gsu.duelingobackend.model.enums.QuestionType;
import by.gsu.duelingobackend.util.ObjectIdSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.bson.types.ObjectId;

public record AudioQuestionResponse(

        @JsonSerialize(using = ObjectIdSerializer.class)
        ObjectId id,
        QuestionType type,
        String questionText,
        String audioUrl
) {
}
