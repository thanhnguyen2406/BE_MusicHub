package musichub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.dto.ChannelDTO.ChannelInfoDTO;
import musichub.dto.ChannelDTO.ChannelJoinPageDTO;
import musichub.dto.PageResponse;
import musichub.dto.RequestRsocket;
import musichub.dto.ResponseAPI;
import musichub.dto.UserDTO.MemberInfoDTO;
import musichub.model.Channel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ChannelService {
    //#region Server Service

    //#region Channel CRUD
    Mono<Channel> createChannelServer(RequestRsocket requestRsocket);
    Mono<Channel> updateChannelServer(RequestRsocket requestRsocket);
    Mono<Void> deleteChannelServer(RequestRsocket requestRsocket);
    //#endregion

    //#region Member actions
    Mono<Channel> joinChannelByIdServer(RequestRsocket requestRsocket);
    Mono<Channel> joinChannelByUrlServer(RequestRsocket requestRsocket);
    Mono<Channel> leaveChannelByIdServer(RequestRsocket requestRsocket);
    Mono<Channel> kickMemberServer(RequestRsocket requestRsocket);
    Mono<Channel> transferOwnershipServer(RequestRsocket requestRsocket);
    //#endregion

    //#region GET methods
    Mono<PageResponse<ChannelJoinPageDTO>> getChannelsServer(RequestRsocket requestRsocket);
    Mono<String> getMyChannelServer(RequestRsocket requestRsocket);
    Mono<ChannelInfoDTO> getChannelByIdServer(RequestRsocket requestRsocket);
    Flux<MemberInfoDTO> searchMemberByDisplayName(RequestRsocket requestRsocket);
    //#endregion

    //#endregion

    //#region Client Service

    //#region Channel CRUD
    Mono<ResponseAPI<Void>> createChannelClient(ChannelDTO channelDTO, String ownerId);
    Mono<ResponseAPI<Void>> updateChannelClient(String channelId, ChannelDTO channelDTO, String ownerId);
    Mono<ResponseAPI<Void>> deleteChannelClient(String channelId, String ownerId);
    //#endregion

    //#region Member actions
    Mono<ResponseAPI<Void>> joinChannelByIdClient(String channelId, String userId);
    Mono<ResponseAPI<Void>> joinChannelByUrlClient(String url, String userId);
    Mono<ResponseAPI<Void>> leaveChannelByIdClient(String channelId, String userId);
    Mono<ResponseAPI<Void>> kickMember(String channelId, String memberId, String ownerId);
    Mono<ResponseAPI<Void>> transferOwnership(String channelId, String newOwnerId, String oldOwnerId);
    //#endregion

    //#region GET methods
    Mono<ResponseAPI<PageResponse<ChannelJoinPageDTO>>> getChannelsClient(int page, int size);
    Mono<ResponseAPI<String>> getMyChannelClient(String ownerId) throws JsonProcessingException;
    Mono<ResponseAPI<ChannelInfoDTO>> getChannelByIdClient(String channelId, String userId);
    Mono<ResponseAPI<List<MemberInfoDTO>>> searchMemberByDisplayName(String channelId, String displayNameKeyword);
    //#endregion

    //#endregion
}
