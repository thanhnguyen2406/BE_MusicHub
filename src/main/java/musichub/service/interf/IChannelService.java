package musichub.service.interf;

import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.dto.ChannelDTO.ChannelInfoDTO;
import musichub.dto.ChannelDTO.ChannelJoinPageDTO;
import musichub.dto.PageResponse;
import musichub.dto.RequestRsocket;
import musichub.dto.ResponseAPI;
import musichub.model.Channel;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface IChannelService {
    //Server
    Mono<Channel> createChannelServer(RequestRsocket requestRsocket);
    Mono<Channel> updateChannelServer(RequestRsocket requestRsocket);
    Mono<Void> deleteChannelServer(RequestRsocket requestRsocket);
    Mono<Channel> joinChannelByIdServer(RequestRsocket requestRsocket);
    Mono<Channel> joinChannelByUrlServer(RequestRsocket requestRsocket);
    Mono<PageResponse<ChannelJoinPageDTO>> getChannelsServer(RequestRsocket requestRsocket);
    Mono<String> getMyChannelServer(RequestRsocket requestRsocket);
    Mono<ChannelInfoDTO> getChannelByIdServer(RequestRsocket requestRsocket);

    //Client
    Mono<ResponseAPI<Void>> createChannelClient(ChannelDTO channelDTO, String userId);
    Mono<ResponseAPI<Void>> updateChannelClient(String channelId, ChannelDTO channelDTO, String userId);
    Mono<ResponseAPI<Void>> deleteChannelClient(String channelId, String userId);
    Mono<ResponseAPI<Void>> joinChannelByIdClient(String channelId, String userId);
    Mono<ResponseAPI<Void>> joinChannelByUrlClient(String url, String userId);
    Mono<ResponseAPI<PageResponse<ChannelJoinPageDTO>>> getChannelsClient(int page, int size);
    Mono<ResponseAPI<String>> getMyChannelClient(String userId);
    Mono<ResponseAPI<ChannelInfoDTO>> getChannelByIdClient(String channelId, String userId);
}
