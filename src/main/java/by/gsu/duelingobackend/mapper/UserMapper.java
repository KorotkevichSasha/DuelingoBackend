package by.gsu.duelingobackend.mapper;

import by.gsu.duelingobackend.dto.response.user.FriendResponse;
import by.gsu.duelingobackend.dto.response.user.UserInDuelResponse;
import by.gsu.duelingobackend.dto.response.user.UserProfileResponse;
import by.gsu.duelingobackend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserMapper {

    FriendResponse toFriendResponse(User user);

    UserProfileResponse toUserProfileResponse(User user);

    @Mapping(target = "userId", source = "id")
    UserInDuelResponse toUserInDuelResponse(User user);
}
