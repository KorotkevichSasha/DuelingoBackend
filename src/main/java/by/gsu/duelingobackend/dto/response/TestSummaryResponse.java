package by.gsu.duelingobackend.dto.response;

import by.gsu.duelingobackend.util.ObjectIdSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.bson.types.ObjectId;

import java.io.Serializable;

public record TestSummaryResponse(

        @JsonSerialize(using = ObjectIdSerializer.class)
        ObjectId id,
        String topic,
        String difficulty,
        boolean isCompleted
) implements Serializable {
}
