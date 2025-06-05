package musichub.service.interf;

import com.fasterxml.jackson.core.JsonProcessingException;
import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.dto.ChannelDTO.ChannelInfoDTO;
import musichub.dto.ChannelDTO.ChannelJoinPageDTO;
import musichub.dto.PageResponse;
import musichub.dto.RequestRsocket;
import musichub.dto.ResponseAPI;
import musichub.dto.UserDTO.MemberInfoDTO;
import musichub.model.Channel;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

public interface IChannelService {
    //Server
    Mono<Channel> createChannelServer(RequestRsocket requestRsocket);
    Mono<Channel> updateChannelServer(RequestRsocket requestRsocket);
    Mono<Void> deleteChannelServer(RequestRsocket requestRsocket);
    Mono<Channel> joinChannelByIdServer(RequestRsocket requestRsocket);
    Mono<Channel> joinChannelByUrlServer(RequestRsocket requestRsocket);
    Mono<Channel> leaveChannelByIdServer(RequestRsocket requestRsocket);
    Mono<PageResponse<ChannelJoinPageDTO>> getChannelsServer(RequestRsocket requestRsocket);
    Mono<String> getMyChannelServer(RequestRsocket requestRsocket);
    Mono<ChannelInfoDTO> getChannelByIdServer(RequestRsocket requestRsocket);
    Flux<MemberInfoDTO> searchMemberByDisplayName(RequestRsocket requestRsocket);
    Mono<Channel> kickMemberServer(RequestRsocket requestRsocket);
    Mono<Channel> transferOwnershipServer(RequestRsocket requestRsocket);

    //Client
    Mono<ResponseAPI<Void>> createChannelClient(ChannelDTO channelDTO, String ownerId);
    Mono<ResponseAPI<Void>> updateChannelClient(String channelId, ChannelDTO channelDTO, String ownerId);
    Mono<ResponseAPI<Void>> deleteChannelClient(String channelId, String ownerId);
    Mono<ResponseAPI<Void>> joinChannelByIdClient(String channelId, String userId);
    Mono<ResponseAPI<Void>> joinChannelByUrlClient(String url, String userId);
    Mono<ResponseAPI<Void>> leaveChannelByIdClient(String channelId, String userId);
    Mono<ResponseAPI<PageResponse<ChannelJoinPageDTO>>> getChannelsClient(int page, int size);
    Mono<ResponseAPI<String>> getMyChannelClient(String ownerId) throws JsonProcessingException;
    Mono<ResponseAPI<ChannelInfoDTO>> getChannelByIdClient(String channelId, String userId);
    Mono<ResponseAPI<List<MemberInfoDTO>>> searchMemberByDisplayName(String channelId, String displayNameKeyword);
    Mono<ResponseAPI<Void>> kickMember(String channelId, String memberId, String ownerId);
    Mono<ResponseAPI<Void>> transferOwnership(String channelId, String newOwnerId, String oldOwnerId);
}
