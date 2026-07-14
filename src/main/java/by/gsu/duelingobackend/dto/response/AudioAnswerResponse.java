package by.gsu.duelingobackend.dto.response;

public record AudioAnswerResponse(

        boolean isCorrect,
        String recognizedText,
        String feedback
) {}
