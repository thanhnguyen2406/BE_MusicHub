package MusicHub.service.interf;

import MusicHub.dto.ChannelDTO.ChannelDTO;
import MusicHub.dto.ResponseAPI;
import MusicHub.model.Channel;
import reactor.core.publisher.Mono;

public interface IChannelService {
    //Server
    Mono<Channel> createChannelServer(ChannelDTO channelDTO);

    //Client
    Mono<ResponseAPI<Void>> createChannelClient(ChannelDTO channelDTO);
}
