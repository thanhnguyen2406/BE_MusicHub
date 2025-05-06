package musichub.controller;

import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.dto.ResponseAPI;
import musichub.service.interf.IChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/channels")
public class ChannelClientController {
    private final IChannelService channelService;

    @PostMapping("/create")
    public Mono<ResponseAPI<Void>> createChannel(@RequestBody ChannelDTO channelDTO, Principal principal) {
        return channelService.createChannelClient(channelDTO, principal.getName());
    }

    @PutMapping("/update")
    public Mono<ResponseAPI<Void>> updateChannel(@RequestBody ChannelDTO channelDTO, Principal principal) {
        return channelService.updateChannelClient(channelDTO, principal.getName());
    }

    @DeleteMapping("/delete/{channelId}")
    public Mono<ResponseAPI<Void>> deleteChannel(@PathVariable String channelId, Principal principal) {
        return channelService.deleteChannelClient(channelId, principal.getName());
    }

    @PostMapping("/id/{channelId}/join")
    public Mono<ResponseAPI<Void>> joinChannelById(@PathVariable String channelId, Principal principal) {
        return channelService.joinChannelByIdClient(channelId, principal.getName());
    }

    @PostMapping("/url/{url}/join")
    public Mono<ResponseAPI<Void>> joinChannelByUrl(@PathVariable String url, Principal principal) {
        return channelService.joinChannelByUrlClient(url, principal.getName());
    }
}
