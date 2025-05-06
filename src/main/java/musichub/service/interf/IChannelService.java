package musichub.service.interf;

import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.dto.RequestRsocket;
import musichub.dto.ResponseAPI;
import musichub.model.Channel;
import reactor.core.publisher.Mono;

public interface IChannelService {
    //Server
    Mono<Channel> createChannelServer(RequestRsocket requestRsocket);
    Mono<Channel> updateChannelServer(RequestRsocket requestRsocket);
    Mono<Void> deleteChannelServer(RequestRsocket requestRsocket);
    Mono<Channel> joinChannelByIdServer(RequestRsocket requestRsocket);
    Mono<Channel> joinChannelByUrlServer(RequestRsocket requestRsocket);

    //Client
    Mono<ResponseAPI<Void>> createChannelClient(ChannelDTO channelDTO, String userId);
    Mono<ResponseAPI<Void>> updateChannelClient(String channelId, ChannelDTO channelDTO, String userId);
    Mono<ResponseAPI<Void>> deleteChannelClient(String channelId, String userId);
    Mono<ResponseAPI<Void>> joinChannelByIdClient(String channelId, String userId);
    Mono<ResponseAPI<Void>> joinChannelByUrlClient(String url, String userId);
}
