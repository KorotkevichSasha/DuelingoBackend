package by.gsu.duelingobackend.util;

import java.util.List;

public final class Constants {

    private Constants() {
    }

    // Security
    public static final List<String> PUBLIC_URLS = List.of(
            "/auth/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/ws/**",
            "/actuator/prometheus"
    );

    public static final List<String> ADMIN_URLS = List.of(
            "/admin/**"
    );

    public static final List<String> ALLOWED_METHODS = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

    public static final List<String> ALLOWED_HEADERS = List.of("*");

    // exception messages

    // user
    public static final String USER_ALREADY_EXISTS_BY_USERNAME_ERR_MSG = "User with username %s already exists";
    public static final String USER_ALREADY_EXISTS_BY_EMAIL_ERR_MSG = "User with email %s already exists";
    public static final String USER_NOT_FOUND_BY_ID_ERR_MSG = "User with id %s not found";
    public static final String USER_NOT_FOUND_BY_USERNAME_ERR_MSG = "User with username %s not found";

    // token
    public static final String INVALID_REFRESH_TOKEN_ERR_MSG = "Invalid Refresh Token";

    // tests
    public static final String TEST_NOT_FOUND_BY_ID_ERR_MSG = "Test with id %s not found";

    // questions
    public static final String QUESTION_NOT_FOUND_BY_ID_ERR_MSG = "Question with id %s not found";

    // words
    public static final String WORD_NOT_FOUND_BY_ID_ERR_MSG = "Word with id %s not found";
    public static final String USER_ALREADY_ADDED_WORD_ERR_MSG = "User already added %s";

    // Relationship exceptions
    public static final String FRIEND_REQUEST_ALREADY_EXISTS_ERR_MSG = "Friend request from user %s to user %s already exists.";
    public static final String RELATIONSHIP_NOT_FOUND_ERR_MSG = "Relationship with ID %s not found.";
    public static final String ONLY_PENDING_REQUESTS_CAN_BE_ACCEPTED_ERR_MSG = "Only pending friend requests can be accepted. Request ID: %s.";
    public static final String ONLY_PENDING_REQUESTS_CAN_BE_REJECTED_ERR_MSG = "Only pending friend requests can be rejected. Request ID: %s.";
    public static final String ONLY_BLOCKED_RELATIONSHIPS_CAN_BE_UNBLOCKED_ERR_MSG = "Only blocked relationships can be unblocked. Block ID: %s.";
    public static final String BLOCK_RELATIONSHIP_NOT_FOUND_ERR_MSG = "Blocked relationship with ID %s not found.";
    public static final String SELF_FRIEND_REQUEST_NOT_ALLOWED_ERR_MSG = "User with ID %s cannot send a friend request to themselves.";
    public static final String SELF_BLOCK_NOT_ALLOWED_ERR_MSG = "User with ID %s cannot block themselves.";

    // achievements
    public static final String ACHIEVEMENT_NOT_FOUND_BY_ID_ERR_MSG = "Achievement with id %s not found";

    public static final String INVALID_IMAGE_TYPE_ERR_MSG = "Invalid image type %s";
}
