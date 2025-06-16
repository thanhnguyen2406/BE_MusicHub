package musichub.controller;

import musichub.dto.ChannelDTO.ChannelInfoDTO;
import musichub.dto.ChannelDTO.ChannelJoinPageDTO;
import musichub.dto.PageResponse;
import musichub.dto.RequestRsocket;
import musichub.dto.UserDTO.MemberInfoDTO;
import musichub.model.Channel;
import musichub.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChannelServerController {
    private final ChannelService channelService;

    public static final String CREATE = "channel.create";
    public static final String UPDATE = "channel.update";
    public static final String DELETE = "channel.delete";
    public static final String JOIN_BY_ID = "channel.joinById";
    public static final String JOIN_BY_URL = "channel.joinByUrl";
    public static final String LEAVE_BY_ID = "channel.leaveById";
    public static final String GET_CHANNELS = "channel.getChannels";
    public static final String GET_MY_CHANNEL = "channel.getMyChannel";
    public static final String GET_CHANNEL_BY_ID = "channel.getChannelById";
    public static final String SEARCH_MEMBER = "channel.searchMemberByDisplayName";
    public static final String KICK_MEMBER = "channel.kickMember";
    public static final String TRANSFER_OWNERSHIP = "channel.transferOwnership";

    @MessageMapping(CREATE)
    public Mono<Channel> createChannel(@Payload RequestRsocket requestRsocket) {
        return channelService.createChannelServer(requestRsocket);
    }

    @MessageMapping(UPDATE)
    public Mono<Channel> updateChannel(@Payload RequestRsocket requestRsocket) {
        return channelService.updateChannelServer(requestRsocket);
    }

    @MessageMapping(DELETE)
    public Mono<Void> deleteChannel(@Payload RequestRsocket requestRsocket) {
        return channelService.deleteChannelServer(requestRsocket);
    }

    @MessageMapping(JOIN_BY_ID)
    public Mono<Channel> joinChannelById(@Payload RequestRsocket requestRsocket) {
        return channelService.joinChannelByIdServer(requestRsocket);
    }

    @MessageMapping(JOIN_BY_URL)
    public Mono<Channel> joinChannelByUrl(@Payload RequestRsocket requestRsocket) {
        return channelService.joinChannelByUrlServer(requestRsocket);
    }

    @MessageMapping(LEAVE_BY_ID)
    public Mono<Channel> leaveChannelById(@Payload RequestRsocket requestRsocket) {
        return channelService.leaveChannelByIdServer(requestRsocket);
    }

    @MessageMapping(KICK_MEMBER)
    public Mono<Channel> kickMember(@Payload RequestRsocket requestRsocket) {
        return channelService.kickMemberServer(requestRsocket);
    }

    @MessageMapping(TRANSFER_OWNERSHIP)
    public Mono<Channel> transferOwnership(@Payload RequestRsocket requestRsocket) {
        return channelService.transferOwnershipServer(requestRsocket);
    }

    @MessageMapping(GET_CHANNELS)
    public Mono<PageResponse<ChannelJoinPageDTO>> getChannels(@Payload RequestRsocket requestRsocket) {
        return channelService.getChannelsServer(requestRsocket);
    }

    @MessageMapping(GET_MY_CHANNEL)
    public Mono<String> getMyChannel(@Payload RequestRsocket request) {
        System.out.println("getMyChannel received");
        return channelService.getMyChannelServer(request);
    }

    @MessageMapping(GET_CHANNEL_BY_ID)
    public Mono<ChannelInfoDTO> getChannelById(@Payload RequestRsocket requestRsocket) {
        return channelService.getChannelByIdServer(requestRsocket);
    }

    @MessageMapping(SEARCH_MEMBER)
    public Flux<MemberInfoDTO> searchMemberByDisplayName(@Payload RequestRsocket requestRsocket) {
        return channelService.searchMemberByDisplayName(requestRsocket);
    }

    @MessageMapping("test")
    public Mono<Map<String, String>> test() {
        System.out.println("Received request to route: test");

        return Mono.just(Map.of("data", "test"));
    }

    @MessageMapping("route")
    public Flux<String> getStreamOfStrings(@Payload String message) {
        System.out.println("Received message: " + message);
        return Flux.just("response1", "response2", "response3")
                .delayElements(Duration.ofSeconds(1));
    }
}