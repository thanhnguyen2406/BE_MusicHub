package musichub.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    //401: Unauthenticated errors
    UNAUTHENTICATED_USERNAME_PASSWORD(401, "Please check username or password again"),
    UNAUTHENTICATED_USERNAME(401, "Please check your username again"),
    UNAUTHENTICATED_USERNAME_DOMAIN(401, "Please enter email ends with @gmail.com"),
    UNAUTHENTICATED_CHANNEL_OWNER(401, "You are not the owner of this channel"),
    UNAUTHENTICATED_CHANNEL_ADDER(401, "Channel's owner is not allowing you to add song"),
    UNAUTHENTICATED_CHANNEL_MEMBER(401, "You are not a member of this channel"),
    UNAUTHENTICATED_ACTION(401, "You don't have permission to do this action"),

    //400: Invalid resource errors
    //400: Invalid keycloak
    PASSWORD_IS_EMPTY(400, "Password is empty"),
    KEYCLOAK_FAIL(400, "Keycloak creation is failed"),
    //400: Invalid channel resource
    CHANNEL_IS_FULL(400, "Channel is full"),
    CANNOT_KICK_OWNER(400, "You cannot kick yourself, the owner of this channel"),
    //400: Invalid song resource
    INVALID_VOTE_TYPE(400, "Invalid vote type"),

    //404: Resource not found errors
    USER_NOT_FOUND(404, "User not found"),
    USER_NOT_IN_CHANNEL(404, "User is not in this channel"),
    CHANNEL_NOT_FOUND(404, "Channel not found"),
    SONG_NOT_FOUND(404, "Song not found"),
    SONG_NOT_FOUND_CHANNEL(404, "This song is not found in this channel"),

    //409: Resource existed errors
    USER_EXISTED(409, "User already existed"),
    USER_ALREADY_IN_CHANNEL(409, "User already in channel");

    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
