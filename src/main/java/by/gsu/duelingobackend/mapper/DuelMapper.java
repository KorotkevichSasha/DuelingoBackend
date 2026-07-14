package by.gsu.duelingobackend.mapper;

import by.gsu.duelingobackend.dto.response.DuelInHistoryResponse;
import by.gsu.duelingobackend.dto.response.DuelResponse;
import by.gsu.duelingobackend.model.Duel;
import by.gsu.duelingobackend.model.document.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {UserMapper.class}
)
public interface DuelMapper {

    @Mapping(target = "questions", source = "questions")
    @Mapping(target = "player1", source = "duel.player1")
    @Mapping(target = "player2", source = "duel.player2")
    DuelResponse toResponse(Duel duel, List<Question> questions);

    DuelInHistoryResponse toDuelInHistoryResponse(Duel duel);
}
