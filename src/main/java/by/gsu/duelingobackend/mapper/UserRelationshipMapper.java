package by.gsu.duelingobackend.mapper;

import by.gsu.duelingobackend.dto.response.RelationshipResponse;
import by.gsu.duelingobackend.model.UserRelationship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserRelationshipMapper {

    @Mapping(target = "fromUserId", source = "fromUser.id")
    @Mapping(target = "fromUsername", source = "fromUser.username")
    @Mapping(target = "fromAvatarUrl", source = "fromUser.avatarUrl")
    @Mapping(target = "toUserId", source = "toUser.id")
    RelationshipResponse toResponse(UserRelationship userRelationship);
}
