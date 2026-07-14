package by.gsu.duelingobackend.mapper;

import by.gsu.duelingobackend.dto.response.UserAchievementResponse;
import by.gsu.duelingobackend.model.UserAchievement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserAchievementMapper {

//    @Mapping(target = "achievementId", source = "id")
//    UserAchievementResponse toResponse(UserAchievement userAchievement);
}
