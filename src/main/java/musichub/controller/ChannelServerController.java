package musichub.controller;

import musichub.dto.ChannelDTO.ChannelInfoDTO;
import musichub.dto.ChannelDTO.ChannelJoinPageDTO;
import musichub.dto.PageResponse;
import musichub.dto.RequestRsocket;
import musichub.model.Channel;
import musichub.service.interf.IChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

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

    @MessageMapping("channel.getChannels")
    public Mono<PageResponse<ChannelJoinPageDTO>> getChannels(@Payload RequestRsocket requestRsocket) {
        return channelService.getChannelsServer(requestRsocket);
    }

    @MessageMapping("channel.getMyChannel")
    public Mono<String> getMyChannel(@Payload RequestRsocket request) {
        return channelService.getMyChannelServer(request);
    }

    @MessageMapping("channel.getChannelById")
    public Mono<ChannelInfoDTO> getChannelById(@Payload RequestRsocket requestRsocket) {
        return channelService.getChannelByIdServer(requestRsocket);
    }

    @MessageMapping("test")
    public Mono<Map<String, String>> test() {
        System.out.println("📥 Received request to route: test");
        return Mono.just(Map.of("data", "test"));
    }
}
