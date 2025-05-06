package musichub.controller;

import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.dto.RequestRsocket;
import musichub.model.Channel;
import musichub.service.interf.IChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class ChannelServerController {
    private final IChannelService channelService;

    @MessageMapping("channel.create")
    public Mono<Channel> createChannel(RequestRsocket requestRsocket) {
        return channelService.createChannelServer(requestRsocket);
    }

    @MessageMapping("channel.update")
    public Mono<Channel> updateChannel(RequestRsocket requestRsocket) {
        return channelService.updateChannelServer(requestRsocket);
    }

    @MessageMapping("channel.delete")
    public Mono<Void> deleteChannel(RequestRsocket requestRsocket) {
        return channelService.deleteChannelServer(requestRsocket);
    }

    @MessageMapping("channel.joinById")
    public Mono<Channel> joinChannelById(RequestRsocket requestRsocket) {
        return channelService.joinChannelByIdServer(requestRsocket);
    }

    @MessageMapping("channel.joinByUrl")
    public Mono<Channel> joinChannelByUrl(RequestRsocket requestRsocket) {
        return channelService.joinChannelByUrlServer(requestRsocket);
    }
}
