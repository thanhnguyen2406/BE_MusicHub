package musichub.common;

import musichub.exception.ErrorCode;
import musichub.model.Channel;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.Map;

public class ChannelPermissionUtil {
    public static Mono<Void> requireOwner(Channel channel, String userId) {
        return AssertUtil.isTrueMono(channel.isOwner(userId), ErrorCode.UNAUTHENTICATED_CHANNEL_OWNER);
    }

    public static Mono<Void> requireNotKickingOwner(Channel channel, String userId) {
        return AssertUtil.isTrueMono(!channel.isOwner(userId), ErrorCode.CANNOT_KICK_OWNER);
    }

    public static Mono<Void> requireMember(Channel channel, String userId) {
        return AssertUtil.isTrueMono(channel.isContainMember(userId), ErrorCode.USER_NOT_IN_CHANNEL);
    }

    public static Mono<Void> requireNotMember(Channel channel, String userId) {
        return AssertUtil.isTrueMono(!channel.isContainMember(userId), ErrorCode.USER_ALREADY_IN_CHANNEL);
    }

    public static Mono<Void> requireChannelNotFull(Channel channel) {
        Map<String, LocalTime> members = channel.getMembers();
        return AssertUtil.isTrueMono(members.size() < channel.getMaxUsers(), ErrorCode.CHANNEL_IS_FULL);
    }
}

