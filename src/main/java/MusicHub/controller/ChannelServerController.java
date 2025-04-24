package MusicHub.controller;

import MusicHub.dto.ChannelDTO.ChannelDTO;
import MusicHub.model.Channel;
import MusicHub.service.interf.IChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class ChannelServerController {
    private final IChannelService channelService;

    @MessageMapping("channel.create")
    public Mono<Channel> createChannel(Mono<ChannelDTO> requestMono) {
        return requestMono.flatMap(channelService::createChannelServer);
    }
}
