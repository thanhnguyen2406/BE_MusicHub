package MusicHub.service.implement;

import MusicHub.dto.ChannelDTO.ChannelDTO;
import MusicHub.dto.RequestRsocket;
import MusicHub.dto.ResponseAPI;
import MusicHub.mapper.ChannelMapper;
import MusicHub.model.Channel;
import MusicHub.repository.ChannelRepository;
import MusicHub.service.interf.IChannelService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ChannelService implements IChannelService {
    ChannelRepository channelRepository;
    ChannelMapper channelMapper;
    RSocketRequester rSocketRequester;

    //SERVER SERVICE

    @Override
    public Mono<Channel> createChannelServer(RequestRsocket requestRsocket) {
        return Mono.fromCallable(() -> {
                    Channel newChannel = channelMapper.toChannel(requestRsocket.getPayloadAs("channelDTO", ChannelDTO.class));
                    newChannel.setCreatedAt(LocalDateTime.now());
                    newChannel.setCreatedBy(requestRsocket.getPayloadAs("userId", String.class));
                    return newChannel;
                })
                .flatMap(channelRepository::save);
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
}
