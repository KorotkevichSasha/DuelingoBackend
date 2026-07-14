package by.gsu.duelingobackend.dto.response;

import by.gsu.duelingobackend.util.ObjectIdSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.List;

@Builder
public record TestDetailedResponse (

        @JsonSerialize(using = ObjectIdSerializer.class)
        ObjectId id,
        String topic,
        String difficulty,
        List<QuestionDetailedResponse> questions
) implements Serializable {
}
