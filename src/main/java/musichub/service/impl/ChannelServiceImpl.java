package musichub.service.impl;

import musichub.common.ChannelPermissionUtil;
import musichub.common.ResponseUtil;
import musichub.controller.ChannelServerController;
import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.dto.ChannelDTO.ChannelInfoDTO;
import musichub.dto.ChannelDTO.ChannelJoinPageDTO;
import musichub.dto.PageResponse;
import musichub.dto.RequestRsocket;
import musichub.dto.ResponseAPI;
import musichub.dto.UserDTO.MemberInfoDTO;
import musichub.exception.AppException;
import musichub.exception.ErrorCode;
import musichub.mapper.ChannelMapper;
import musichub.mapper.UserMapper;
import musichub.model.Channel;
import musichub.repository.ChannelRepository;
import musichub.repository.UserRepository;
import musichub.service.ChannelService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ChannelServiceImpl implements ChannelService {
    ChannelRepository channelRepository;
    UserRepository userRepository;
    ChannelMapper channelMapper;
    UserMapper userMapper;
    RSocketRequester rSocketRequester;

    public static final String CHANNEL_ID = "channelId";
    private static final String CHANNEL_DTO = "channelDTO";
    private static final String NEW_OWNER_ID = "newOwnerId";
    private static final String OLD_OWNER_ID = "oldOwnerId";
    private static final String USER_ID = "userId";
    private static final String MEMBER_ID = "memberId";
    private static final String URL = "url";
    private static final String PAGE = "page";
    private static final String SIZE = "size";
    private static final String DISPLAY_NAME_KEYWORD = "displayNameKeyword";

    //#region Server Service

    //#region Channel CRUD
    @Override
    public Mono<Channel> createChannelServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);
        ChannelDTO channelDTO = requestRsocket.getPayloadAs(CHANNEL_DTO, ChannelDTO.class);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .map(user -> channelMapper.toChannel(channelDTO, user))
                .flatMap(channelRepository::save);
    }

    @Override
    public Mono<Channel> updateChannelServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);
        ChannelDTO channelDTO = requestRsocket.getPayloadAs(CHANNEL_DTO, ChannelDTO.class);

        return channelRepository.findById(channelDTO.getId())
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    updateChannel(channel, channelDTO);

                    return ChannelPermissionUtil
                            .requireMember(channel, userId)
                            .then(channelRepository.save(channel));
                });
    }

    private void updateChannel (Channel channel, ChannelDTO channelDTO) {
        channel.setName(channelDTO.getName());
        channel.setUrl(channelDTO.getUrl());
        channel.setTagList(channelDTO.getTagList());
        channel.setDescription(channelDTO.getDescription());
        channel.setPassword(channelDTO.getPassword());
        channel.setIsLocked(channelDTO.getPassword() != null && !channelDTO.getPassword().isEmpty());
        channel.setMaxUsers(channelDTO.getMaxUsers());
        channel.setAllowOthersToManageSongs(channelDTO.getAllowOthersToManageSongs());
        channel.setAllowOthersToControlPlayback(channelDTO.getAllowOthersToControlPlayback());
        channel.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public Mono<Void> deleteChannelServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);
        String channelId = requestRsocket.getPayloadAs(CHANNEL_ID, String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel ->
                        ChannelPermissionUtil
                                .requireOwner(channel, userId)
                                .then(channelRepository.delete(channel)));
    }
    //#endregion

    //#region Member actions
    @Override
    public Mono<Channel> joinChannelByIdServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);
        String channelId = requestRsocket.getPayloadAs(CHANNEL_ID, String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> validateAndJoin(channel, userId));
    }

    @Override
    public Mono<Channel> joinChannelByUrlServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);
        String url = requestRsocket.getPayloadAs(URL, String.class);

        return channelRepository.findByUrl(url)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> validateAndJoin(channel, userId));
    }

    private Mono<Channel> validateAndJoin(Channel channel, String userId) {
        return ChannelPermissionUtil
                .requireChannelNotFull(channel)
                .then(ChannelPermissionUtil.requireNotMember(channel, userId))
                .then(Mono.defer(() -> {
                    channel.addMember(userId);
                    return channelRepository.save(channel);
                }));
    }

    @Override
    public Mono<Channel> leaveChannelByIdServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);
        String channelId = requestRsocket.getPayloadAs(CHANNEL_ID, String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel ->
                    ChannelPermissionUtil
                            .requireMember(channel, userId)
                            .then(Mono.defer(() -> {
                                channel.removeMember(userId);
                                return channelRepository.save(channel);
                            }))
                );
    }

    @Override
    public Mono<Channel> kickMemberServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs(CHANNEL_ID, String.class);
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);
        String memberId = requestRsocket.getPayloadAs(MEMBER_ID, String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel ->
                        ChannelPermissionUtil
                                .requireOwner(channel, userId)
                                .then(ChannelPermissionUtil.requireMember(channel, memberId))
                                .then(ChannelPermissionUtil.requireNotKickingOwner(channel, memberId))
                                .then(Mono.defer(() -> {
                                    channel.removeMember(memberId);
                                    return channelRepository.save(channel);
                                }))
                );
    }

    @Override
    public Mono<Channel> transferOwnershipServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs(CHANNEL_ID, String.class);
        String newOwnerId = requestRsocket.getPayloadAs(NEW_OWNER_ID, String.class);
        String oldOwnerId = requestRsocket.getPayloadAs(OLD_OWNER_ID, String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel ->
                        ChannelPermissionUtil
                                .requireOwner(channel, oldOwnerId)
                                .then(ChannelPermissionUtil.requireMember(channel, newOwnerId))
                                .then(Mono.defer(() ->
                                        userRepository.findById(newOwnerId)
                                                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                                                .flatMap(newOwner -> {
                                                    channel.setOwner(newOwner);
                                                    channel.setUpdatedAt(LocalDateTime.now());
                                                    return channelRepository.save(channel);
                                                })
                                )));
    }
    //#endregion

    //#region GET methods
    @Override
    public Mono<PageResponse<ChannelJoinPageDTO>> getChannelsServer(RequestRsocket requestRsocket) {
        int page = requestRsocket.getPayloadAs(PAGE, Integer.class);
        int size = requestRsocket.getPayloadAs(SIZE, Integer.class);

        Mono<List<ChannelJoinPageDTO>> data = channelRepository.findAllWithPage(page, size)
                .map(channelMapper::toChannelJoinPageDTO)
                .collectList();

        Mono<Long> total = channelRepository.count();

        return Mono.zip(data, total)
                .map(tuple -> PageResponse.<ChannelJoinPageDTO>builder()
                        .content(tuple.getT1())
                        .page(page)
                        .size(size)
                        .totalElements(tuple.getT2())
                        .build());
    }

    @Override
    public Mono<String> getMyChannelServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user ->
                        channelRepository.findByMemberId(userId)
                                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                                .map(Channel::getId)
                );
    }

    @Override
    public Mono<ChannelInfoDTO> getChannelByIdServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs(CHANNEL_ID, String.class);
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel ->
                        ChannelPermissionUtil
                                .requireMember(channel, userId)
                                .then(channelMapper.toChannelInfoDTO(channel))
                );
    }

    @Override
    public Flux<MemberInfoDTO> searchMemberByDisplayName(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs(CHANNEL_ID, String.class);
        String displayNameKeyword = requestRsocket.getPayloadAs(DISPLAY_NAME_KEYWORD, String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMapMany(channel -> mapMembersWithFilter(channel, displayNameKeyword));
    }

    private Flux<MemberInfoDTO> mapMembersWithFilter(Channel channel, String keyword) {
        Set<String> memberIds = channel.getMembers().keySet();
        String ownerId = channel.getOwnerId();

        return userMapper.toMembersList(memberIds, ownerId)
                .flatMapMany(Flux::fromIterable)
                .filter(member -> isDisplayNameMatch(member, keyword));
    }

    private boolean isDisplayNameMatch(MemberInfoDTO member, String keyword) {
        return keyword.isBlank() ||
                (member.getDisplayName() != null &&
                        member.getDisplayName().toLowerCase().contains(keyword));
    }
    //#endregion

    //#endregion


    //#region Client Service

    //#region Channel CRUD
    @Override
    public Mono<ResponseAPI<Void>> createChannelClient(ChannelDTO channelDTO, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(CHANNEL_DTO, channelDTO);
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(ChannelServerController.CREATE)
                .data(requestRsocket)
                .retrieveMono(Channel.class)
                .map(channel ->
                        ResponseUtil.success("Channel created successfully")
                );
    }

    @Override
    public Mono<ResponseAPI<Void>> updateChannelClient(String channelId, ChannelDTO channelDTO, String userId) {
        channelDTO.setId(channelId);
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(CHANNEL_DTO, channelDTO);
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(ChannelServerController.UPDATE)
                .data(requestRsocket)
                .retrieveMono(Channel.class)
                .map(channel ->
                        ResponseUtil.success("Channel updated successfully")
                );
    }

    @Override
    public Mono<ResponseAPI<Void>> deleteChannelClient(String channelId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(CHANNEL_ID, channelId);
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(ChannelServerController.DELETE)
                .data(requestRsocket)
                .retrieveMono(Channel.class)
                .then(Mono.fromCallable(() ->
                        ResponseUtil.success("Channel deleted successfully"))
                );
    }
    //#endregion

    //#region Member actions
    @Override
    public Mono<ResponseAPI<Void>> joinChannelByIdClient(String channelId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(CHANNEL_ID, channelId);
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(ChannelServerController.JOIN_BY_ID)
                .data(requestRsocket)
                .retrieveMono(Channel.class)
                .map(channel ->
                        ResponseUtil.success("Joined channel successfully")
                );
    }

    @Override
    public Mono<ResponseAPI<Void>> joinChannelByUrlClient(String url, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(URL, url);
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(ChannelServerController.JOIN_BY_URL)
                .data(url)
                .retrieveMono(Channel.class)
                .map(channel ->
                        ResponseUtil.success("Joined channel successfully")
                );
    }

    @Override
    public Mono<ResponseAPI<Void>> leaveChannelByIdClient(String channelId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(CHANNEL_ID, channelId);
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(ChannelServerController.LEAVE_BY_ID)
                .data(requestRsocket)
                .retrieveMono(Channel.class)
                .map(channel ->
                        ResponseUtil.success("Leaved channel successfully")
                );
    }

    @Override
    public Mono<ResponseAPI<Void>> kickMember(String channelId, String memberId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(CHANNEL_ID, channelId);
        requestRsocket.setPayload(MEMBER_ID, memberId);
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(ChannelServerController.KICK_MEMBER)
                .data(requestRsocket)
                .retrieveMono(Channel.class)
                .map(channel ->
                        ResponseUtil.success("Member kicked successfully")
                );
    }

    @Override
    public Mono<ResponseAPI<Void>> transferOwnership(String channelId, String newOwnerId, String oldOwnerId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(CHANNEL_ID, channelId);
        requestRsocket.setPayload(NEW_OWNER_ID, newOwnerId);
        requestRsocket.setPayload(OLD_OWNER_ID, oldOwnerId);

        return rSocketRequester
                .route(ChannelServerController.TRANSFER_OWNERSHIP)
                .data(requestRsocket)
                .retrieveMono(Channel.class)
                .map(channel ->
                        ResponseUtil.success("Owner transferred successfully")
                );
    }
    //#endregion

    //#region GET methods
    @Override
    public Mono<ResponseAPI<PageResponse<ChannelJoinPageDTO>>> getChannelsClient(int page, int size) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(PAGE, page);
        requestRsocket.setPayload(SIZE, size);

        return rSocketRequester
                .route(ChannelServerController.GET_CHANNELS)
                .data(requestRsocket)
                .retrieveMono(new ParameterizedTypeReference<PageResponse<ChannelJoinPageDTO>>() {})
                .map(channelsPage -> {
                    String message = channelsPage != null? "Fetched channels successfully" : "No channels found";
                    return ResponseUtil.success(channelsPage, message);
                });
    }

    @Override
    public Mono<ResponseAPI<String>> getMyChannelClient(String userId){
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(ChannelServerController.GET_MY_CHANNEL)
                .data(requestRsocket)
                .retrieveMono(String.class)
                .map(channelId ->
                        ResponseUtil.success(channelId, "Fetched my channel successfully")
                );
    }

    @Override
    public Mono<ResponseAPI<ChannelInfoDTO>> getChannelByIdClient(String channelId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(CHANNEL_ID, channelId);
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(ChannelServerController.GET_CHANNEL_BY_ID)
                .data(requestRsocket)
                .retrieveMono(ChannelInfoDTO.class)
                .map(channelInfoDTO ->
                        ResponseUtil.success(channelInfoDTO, "Fetched channel by ID successfully")
                );
    }

    @Override
    public Mono<ResponseAPI<List<MemberInfoDTO>>> searchMemberByDisplayName(String channelId, String displayNameKeyword) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(CHANNEL_ID, channelId);
        requestRsocket.setPayload(DISPLAY_NAME_KEYWORD, displayNameKeyword);

        return rSocketRequester
                .route(ChannelServerController.SEARCH_MEMBER)
                .data(requestRsocket)
                .retrieveFlux(MemberInfoDTO.class)
                .collectList()
                .map(memberList ->
                        ResponseUtil.success(memberList, "Search member by display name successfully")
                );
    }
    //#endregion

    //#endregion
}