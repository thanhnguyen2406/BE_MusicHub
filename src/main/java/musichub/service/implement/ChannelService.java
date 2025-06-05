package musichub.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import musichub.service.interf.IChannelService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ChannelService implements IChannelService {
    ChannelRepository channelRepository;
    UserRepository userRepository;
    ChannelMapper channelMapper;
    UserMapper userMapper;
    RSocketRequester rSocketRequester;

    //SERVER SERVICE

    @Override
    public Mono<Channel> createChannelServer(RequestRsocket requestRsocket) {
        String ownerId = requestRsocket.getPayloadAs("ownerId", String.class);
        ChannelDTO channelDTO = requestRsocket.getPayloadAs("channelDTO", ChannelDTO.class);

        return userRepository.findById(ownerId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .map(user -> channelMapper.toChannel(channelDTO, user))
                .flatMap(channelRepository::save);
    }

    @Override
    public Mono<Channel> updateChannelServer(RequestRsocket requestRsocket) {
        String ownerId = requestRsocket.getPayloadAs("ownerId", String.class);
        ChannelDTO channelDTO = requestRsocket.getPayloadAs("channelDTO", ChannelDTO.class);

        return channelRepository.findById(channelDTO.getId())
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (!Objects.equals(channel.getAddedBy(), ownerId)) {
                        return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_CHANNEL_OWNER));
                    }
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
                    return channelRepository.save(channel);
                });
    }

    @Override
    public Mono<Void> deleteChannelServer(RequestRsocket requestRsocket) {
        String ownerId = requestRsocket.getPayloadAs("ownerId", String.class);
        String channelId = requestRsocket.getPayloadAs("channelId", String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (!Objects.equals(channel.getAddedBy(), ownerId)) {
                        return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_CHANNEL_OWNER));
                    }
                    return channelRepository.delete(channel);
                });
    }

    @Override
    public Mono<Channel> joinChannelByIdServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs("userId", String.class);
        String channelId = requestRsocket.getPayloadAs("channelId", String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (channel.getMembers().size() >= channel.getMaxUsers()) {
                        return Mono.error(new AppException(ErrorCode.CHANNEL_IS_FULL));
                    }
                    if (channel.getMembers().containsKey(userId)) {
                        return Mono.error(new AppException(ErrorCode.USER_ALREADY_IN_CHANNEL));
                    }
                    channel.getMembers().put(userId, LocalTime.now());
                    channel.setUpdatedAt(LocalDateTime.now());
                    return channelRepository.save(channel);
                });
    }

    @Override
    public Mono<Channel> joinChannelByUrlServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs("userId", String.class);
        String url = requestRsocket.getPayloadAs("url", String.class);

        return channelRepository.findByUrl(url)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (channel.getMembers().size() >= channel.getMaxUsers()) {
                        return Mono.error(new AppException(ErrorCode.CHANNEL_IS_FULL));
                    }
                    if (channel.getMembers().containsKey(userId)) {
                        return Mono.error(new AppException(ErrorCode.USER_ALREADY_IN_CHANNEL));
                    }
                    channel.getMembers().put(userId, LocalTime.now());
                    channel.setUpdatedAt(LocalDateTime.now());
                    return channelRepository.save(channel);
                });
    }

    @Override
    public Mono<Channel> leaveChannelByIdServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs("userId", String.class);
        String channelId = requestRsocket.getPayloadAs("channelId", String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (!channel.getMembers().containsKey(userId)) {
                        return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_ACTION));
                    }
                    channel.getMembers().remove(userId);
                    channel.setUpdatedAt(LocalDateTime.now());
                    return channelRepository.save(channel);
                });
    }

    @Override
    public Mono<PageResponse<ChannelJoinPageDTO>> getChannelsServer(RequestRsocket requestRsocket) {
        int page = requestRsocket.getPayloadAs("page", Integer.class);
        int size = requestRsocket.getPayloadAs("size", Integer.class);
        Mono<List<ChannelJoinPageDTO>> data = channelRepository.findAll()
                .skip((long) page * size)
                .take(size)
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
        String ownerId = requestRsocket.getPayloadAs("ownerId", String.class);

        return userRepository.findById(ownerId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user ->
                        channelRepository.findByMemberId(ownerId)
                                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                                .map(Channel::getId)
                );
    }

    @Override
    public Mono<ChannelInfoDTO> getChannelByIdServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs("channelId", String.class);
        String userId = requestRsocket.getPayloadAs("userId", String.class);
        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (!channel.getMembers().containsKey(userId)) {
                        return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_ACTION));
                    }
                    return channelMapper.toChannelInfoDTO(channel);
                });
    }

    @Override
    public Flux<MemberInfoDTO> searchMemberByDisplayName(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs("channelId", String.class);
        String displayNameKeyword = requestRsocket.getPayloadAs("displayNameKeyword", String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMapMany(channel -> {
                    Set<String> memberIds = channel.getMembers().keySet();
                    String ownerId = channel.getOwner().getId();

                    return userMapper.toMembersList(memberIds, ownerId)
                            .flatMapMany(Flux::fromIterable)
                            .filter(member -> {
                                if (displayNameKeyword == null || displayNameKeyword.trim().isEmpty()) {
                                    return true;
                                }
                                return member.getDisplayName() != null &&
                                        member.getDisplayName().toLowerCase().contains(displayNameKeyword.toLowerCase());
                            });
                });
    }

    @Override
    public Mono<Channel> kickMemberServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs("channelId", String.class);
        String ownerId = requestRsocket.getPayloadAs("ownerId", String.class);
        String memberId = requestRsocket.getPayloadAs("memberId", String.class);
        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (!Objects.equals(channel.getAddedBy(), ownerId)) {
                        return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_CHANNEL_OWNER));
                    }
                    if (ownerId.equals(memberId)) {
                        return Mono.error(new AppException(ErrorCode.CANNOT_KICK_OWNER));
                    }
                    if (!channel.getMembers().containsKey(memberId)) {
                        return Mono.error(new AppException(ErrorCode.USER_NOT_IN_CHANNEL));
                    }
                    channel.getMembers().remove(memberId);
                    channel.setUpdatedAt(LocalDateTime.now());
                    return channelRepository.save(channel);
                });
    }

    @Override
    public Mono<Channel> transferOwnershipServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs("channelId", String.class);
        String newOwnerId = requestRsocket.getPayloadAs("newOwnerId", String.class);
        String oldOwnerId = requestRsocket.getPayloadAs("oldOwnerId", String.class);
        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    String currentOwnerId = channel.getOwner().getId();
                    if (!currentOwnerId.equals(oldOwnerId)) {
                        return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_CHANNEL_OWNER));
                    }
                    if (!channel.getMembers().containsKey(newOwnerId)) {
                        return Mono.error(new AppException(ErrorCode.USER_NOT_IN_CHANNEL));
                    }
                    channel.setUpdatedAt(LocalDateTime.now());
                    return userRepository.findById(newOwnerId)
                            .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                            .flatMap(newOwner -> {
                                channel.setOwner(newOwner);
                                return channelRepository.save(channel);
                            });
                });
    }


    //CLIENT SERVICE

    @Override
    public Mono<ResponseAPI<Void>> createChannelClient(ChannelDTO channelDTO, String ownerId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelDTO", channelDTO);
        requestRsocket.setPayload("ownerId", ownerId);
        return rSocketRequester
                .route("channel.create")
                .data(requestRsocket)
                .retrieveMono(Channel.class)
                .map(channel -> ResponseAPI.<Void>builder()
                        .code(200)
                        .message("Channel created successfully")
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Failed to create channel: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> updateChannelClient(String channelId, ChannelDTO channelDTO, String ownerId) {
        channelDTO.setId(channelId);
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelDTO", channelDTO);
        requestRsocket.setPayload("ownerId", ownerId);
        return rSocketRequester
                .route("channel.update")
                .data(requestRsocket)
                .retrieveMono(Channel.class)
                .map(channel -> ResponseAPI.<Void>builder()
                        .code(200)
                        .message("Channel updated successfully")
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Failed to update channel: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> deleteChannelClient(String channelId, String ownerId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("ownerId", ownerId);
        return rSocketRequester
                .route("channel.delete")
                .data(requestRsocket)
                .retrieveMono(Void.class)
                .then(Mono.fromCallable(() -> ResponseAPI.<Void>builder()
                        .code(200)
                        .message("Channel deleted successfully")
                        .build()))
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Failed to deleted channel: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> joinChannelByIdClient(String channelId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("userId", userId);
        return rSocketRequester
                .route("channel.joinById")
                .data(requestRsocket)
                .retrieveMono(String.class)
                .map(channel -> ResponseAPI.<Void>builder()
                        .code(200)
                        .message("Join channel successfully")
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Failed to join channel: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> joinChannelByUrlClient(String url, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("url", url);
        requestRsocket.setPayload("userId", userId);
        return rSocketRequester
                .route("channel.joinByUrl")
                .data(url)
                .retrieveMono(String.class)
                .map(channel -> ResponseAPI.<Void>builder()
                        .code(200)
                        .message("Join channel successfully")
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Failed to join channel: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> leaveChannelByIdClient(String channelId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("userId", userId);
        return rSocketRequester
                .route("channel.leaveById")
                .data(requestRsocket)
                .retrieveMono(String.class)
                .map(channel -> ResponseAPI.<Void>builder()
                        .code(200)
                        .message("Leave channel successfully")
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Failed to leave channel: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<PageResponse<ChannelJoinPageDTO>>> getChannelsClient(int page, int size) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("page", page);
        requestRsocket.setPayload("size", size);
        return rSocketRequester
                .route("channel.getChannels")
                .data(requestRsocket)
                .retrieveMono(new ParameterizedTypeReference<PageResponse<ChannelJoinPageDTO>>() {})
                .map(channelsPage -> ResponseAPI.<PageResponse<ChannelJoinPageDTO>>builder()
                        .code(200)
                        .message(channelsPage != null? "Fetched channels successfully" : "No channels found")
                        .data(channelsPage)
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<PageResponse<ChannelJoinPageDTO>>builder()
                        .code(500)
                        .message("Failed to fetched channels: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<String>> getMyChannelClient(String ownerId) throws JsonProcessingException {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("ownerId", ownerId);
        System.out.println(new ObjectMapper().writeValueAsString(requestRsocket));

        return rSocketRequester
                .route("channel.getMyChannel")
                .data(requestRsocket)
                .retrieveMono(String.class)
                .map(channelId -> ResponseAPI.<String>builder()
                        .code(200)
                        .message("Fetched my channel successfully")
                        .data(channelId)
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<String>builder()
                        .code(500)
                        .message("Failed to fetched my channel: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<ChannelInfoDTO>> getChannelByIdClient(String channelId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("userId", userId);
        return rSocketRequester
                .route("channel.getChannelById")
                .data(requestRsocket)
                .retrieveMono(ChannelInfoDTO.class)
                .map(channel -> ResponseAPI.<ChannelInfoDTO>builder()
                        .code(200)
                        .message("Fetched channel successfully")
                        .data(channel)
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<ChannelInfoDTO>builder()
                        .code(500)
                        .message("Failed to fetched channel: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<List<MemberInfoDTO>>> searchMemberByDisplayName(String channelId, String displayNameKeyword) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("displayNameKeyword", displayNameKeyword);
        return rSocketRequester
                .route("channel.searchMemberByDisplayName")
                .data(requestRsocket)
                .retrieveFlux(MemberInfoDTO.class)
                .collectList()
                .map(memberList -> ResponseAPI.<List<MemberInfoDTO>>builder()
                        .code(200)
                        .message("Search member by display name successfully")
                        .data(memberList)
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<List<MemberInfoDTO>>builder()
                        .code(500)
                        .message("Failed to search member by display name: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> kickMember(String channelId, String memberId, String ownerId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("memberId", memberId);
        requestRsocket.setPayload("ownerId", ownerId);
        return rSocketRequester
                .route("channel.kickMember")
                .data(requestRsocket)
                .retrieveMono(Void.class)
                .then(Mono.fromCallable(() -> ResponseAPI.<Void>builder()
                        .code(200)
                        .message("Member kicked successfully")
                        .build()))
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Failed to kick member: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> transferOwnership(String channelId, String newOwnerId, String oldOwnerId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("newOwnerId", newOwnerId);
        requestRsocket.setPayload("oldOwnerId", oldOwnerId);
        return rSocketRequester
                .route("channel.transferOwnership")
                .data(requestRsocket)
                .retrieveMono(Channel.class)
                .map(channel -> ResponseAPI.<Void>builder()
                        .code(200)
                        .message("Ownership transferred successfully")
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Failed to transfer ownership: " + e.getMessage())
                        .build()));
    }
}