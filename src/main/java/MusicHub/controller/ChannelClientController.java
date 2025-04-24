package MusicHub.controller;

import MusicHub.dto.ChannelDTO.ChannelDTO;
import MusicHub.dto.ResponseAPI;
import MusicHub.service.interf.IChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/channels")
public class ChannelClientController {
    private final IChannelService channelService;

    @PostMapping("/create")
    public Mono<ResponseAPI<Void>> createChannel(@RequestBody ChannelDTO channelDTO) {
        return channelService.createChannelClient(channelDTO);
    }
}
