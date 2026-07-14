package by.gsu.duelingobackend.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RelationshipRequest(

        @NotNull(message = "Receiver id can not be null")
        UUID toUserId
) {
}
