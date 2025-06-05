package musichub.controller;

import musichub.dto.ChannelDTO.ChannelInfoDTO;
import musichub.dto.ChannelDTO.ChannelJoinPageDTO;
import musichub.dto.PageResponse;
import musichub.dto.RequestRsocket;
import musichub.dto.UserDTO.MemberInfoDTO;
import musichub.model.Channel;
import musichub.service.interf.IChannelService;
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
    private final IChannelService channelService;

    @MessageMapping("channel.create")
    public Mono<Channel> createChannel(@Payload RequestRsocket requestRsocket) {
        return channelService.createChannelServer(requestRsocket);
    }

    @MessageMapping("channel.update")
    public Mono<Channel> updateChannel(@Payload RequestRsocket requestRsocket) {
        return channelService.updateChannelServer(requestRsocket);
    }

    @MessageMapping("channel.delete")
    public Mono<Void> deleteChannel(@Payload RequestRsocket requestRsocket) {
        return channelService.deleteChannelServer(requestRsocket);
    }

    @MessageMapping("channel.joinById")
    public Mono<Channel> joinChannelById(@Payload RequestRsocket requestRsocket) {
        return channelService.joinChannelByIdServer(requestRsocket);
    }

    @MessageMapping("channel.joinByUrl")
    public Mono<Channel> joinChannelByUrl(@Payload RequestRsocket requestRsocket) {
        return channelService.joinChannelByUrlServer(requestRsocket);
    }

    @MessageMapping("channel.leaveById")
    public Mono<Channel> leaveChannelById(@Payload RequestRsocket requestRsocket) {
        return channelService.leaveChannelByIdServer(requestRsocket);
    }

    @MessageMapping("channel.getChannels")
    public Mono<PageResponse<ChannelJoinPageDTO>> getChannels(@Payload RequestRsocket requestRsocket) {
        return channelService.getChannelsServer(requestRsocket);
    }

    @MessageMapping("channel.getMyChannel")
    public Mono<String> getMyChannel(@Payload RequestRsocket request) {
        System.out.println("getMyChannel received");
        return channelService.getMyChannelServer(request);
    }

    @MessageMapping("channel.getChannelById")
    public Mono<ChannelInfoDTO> getChannelById(@Payload RequestRsocket requestRsocket) {
        return channelService.getChannelByIdServer(requestRsocket);
    }

    @MessageMapping("channel.searchMemberByDisplayName")
    public Flux<MemberInfoDTO> searchMemberByDisplayName(@Payload RequestRsocket requestRsocket) {
        return channelService.searchMemberByDisplayName(requestRsocket);
    }

    @MessageMapping("channel.kickMember")
    public Mono<Channel> kickMember(@Payload RequestRsocket requestRsocket) {
        return channelService.kickMemberServer(requestRsocket);
    }

    @MessageMapping("channel.transferOwnership")
    public Mono<Channel> transferOwnership(@Payload RequestRsocket requestRsocket) {
        return channelService.transferOwnershipServer(requestRsocket);
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