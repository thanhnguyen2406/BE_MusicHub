package musichub.service.implement;

import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.dto.ChannelDTO.ChannelInfoDTO;
import musichub.dto.ChannelDTO.ChannelJoinPageDTO;
import musichub.dto.PageResponse;
import musichub.dto.RequestRsocket;
import musichub.dto.ResponseAPI;
import musichub.exception.AppException;
import musichub.exception.ErrorCode;
import musichub.mapper.ChannelMapper;
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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ChannelService implements IChannelService {
    ChannelRepository channelRepository;
    UserRepository userRepository;
    ChannelMapper channelMapper;
    RSocketRequester rSocketRequester;

    //SERVER SERVICE

    @Override
    public Mono<Channel> createChannelServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs("userId", String.class);
        ChannelDTO channelDTO = requestRsocket.getPayloadAs("channelDTO", ChannelDTO.class);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .map(user -> channelMapper.toChannel(channelDTO, user))
                .flatMap(channelRepository::save);
    }

    @Override
    public Mono<Channel> updateChannelServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs("userId", String.class);
        ChannelDTO channelDTO = requestRsocket.getPayloadAs("channelDTO", ChannelDTO.class);

        return channelRepository.findById(channelDTO.getId())
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (!Objects.equals(channel.getAddedBy(), userId)) {
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
        String userId = requestRsocket.getPayloadAs("userId", String.class);
        String channelId = requestRsocket.getPayloadAs("channelId", String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (!Objects.equals(channel.getAddedBy(), userId)) {
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
        String userId = requestRsocket.getPayloadAs("userId", String.class);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user ->
                        channelRepository.findByOwner(user)
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


    //CLIENT SERVICE

    @Override
    public Mono<ResponseAPI<Void>> createChannelClient(ChannelDTO channelDTO, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelDTO", channelDTO);
        requestRsocket.setPayload("userId", userId);
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
    public Mono<ResponseAPI<Void>> updateChannelClient(String channelId, ChannelDTO channelDTO, String userId) {
        channelDTO.setId(channelId);
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelDTO", channelDTO);
        requestRsocket.setPayload("userId", userId);
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
    public Mono<ResponseAPI<Void>> deleteChannelClient(String channelId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("userId", userId);
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
    public Mono<ResponseAPI<String>> getMyChannelClient(String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("userId", userId);
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
}