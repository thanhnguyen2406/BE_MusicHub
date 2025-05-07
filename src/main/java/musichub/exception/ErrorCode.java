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

    //400: Invalid encoded device
    //Google Access Token INVALID
    TOKEN_FETCHED_FAIL(400, "Fail to fetch access token"),
    USERINFO_FETCHED_FAIL(400, "Fail to fetch user info"),
    UNAUTHENTICATED_LOGIN(400, "This mail must be logan through google service"),
    //400: Invalid keycloak
    PASSWORD_IS_EMPTY(400, "Password is empty"),
    KEYCLOAK_FAIL(400, "Keycloak creation is failed"),
    //400: Invalid channel resource
    CHANNEL_IS_FULL(400, "Channel is full"),
    //400: Invalid song resource

    //404: Resource not found errors
    USER_NOT_FOUND(404, "User not found"),
    CHANNEL_NOT_FOUND(404, "Channel not found"),
    SONG_NOT_FOUND(404, "Song not found"),
    SONG_NOT_FOUND_CHANNEL(404, "This song is not found in this channel"),

    //409: Resource existed errors
    USER_EXISTED(409, "User already existed"),
    USER_ALREADY_IN_CHANNEL(409, "User already in channel"),;

    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
