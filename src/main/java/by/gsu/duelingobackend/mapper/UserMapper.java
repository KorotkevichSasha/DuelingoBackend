package by.gsu.duelingobackend.mapper;

import by.gsu.duelingobackend.dto.response.user.FriendResponse;
import by.gsu.duelingobackend.dto.response.user.UserInDuelResponse;
import by.gsu.duelingobackend.dto.response.user.UserProfileResponse;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.service.EconomyService;
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

    default UserProfileResponse toUserProfileResponse(User user) {
        var nextCharge = user.getRushCharges() >= EconomyService.MAX_RUSH_CHARGES
                ? null : user.getRushChargesUpdatedAt().plusMinutes(EconomyService.MINUTES_PER_CHARGE);
        var league = by.gsu.duelingobackend.model.enums.LeagueTier.forPoints(user.getPoints())
                .response(user.getPoints());
        return new UserProfileResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getPoints(),
                user.getAvatarUrl(), user.getLastLogin(),
                new by.gsu.duelingobackend.dto.response.EconomyResponse(
                        user.getGold(), user.getRushCharges(), EconomyService.MAX_RUSH_CHARGES,
                        nextCharge, EconomyService.MINUTES_PER_CHARGE, user.getPoints(), league)
        );
    }

    @Mapping(target = "userId", source = "id")
    UserInDuelResponse toUserInDuelResponse(User user);
}
