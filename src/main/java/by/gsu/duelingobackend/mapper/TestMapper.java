package by.gsu.duelingobackend.mapper;

import by.gsu.duelingobackend.dto.response.TestDetailedResponse;
import by.gsu.duelingobackend.dto.response.TestSummaryResponse;
import by.gsu.duelingobackend.model.document.Test;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TestMapper {

    TestSummaryResponse toSummaryResponse(Test test, boolean isCompleted);

    @Mapping(target = "isCompleted", ignore = true)
    TestSummaryResponse toSummaryResponse(Test test);

    TestDetailedResponse toDetailedResponse(Test test);
}
