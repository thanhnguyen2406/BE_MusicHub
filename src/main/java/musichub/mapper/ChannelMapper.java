package musichub.mapper;

import lombok.RequiredArgsConstructor;
import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.dto.ChannelDTO.ChannelInfoDTO;
import musichub.dto.ChannelDTO.ChannelJoinPageDTO;
import musichub.dto.SongDTO.SongDTO;
import musichub.dto.UserDTO.MemberInfoDTO;
import musichub.model.Channel;
import musichub.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChannelMapper {
    private final SongMapper songMapper;
    private final UserMapper userMapper;

    public Channel toChannel(ChannelDTO channelDTO, User user) {
        PasswordEncoder encoder = new BCryptPasswordEncoder(5);
        return Channel.builder()
                .name(channelDTO.getName())
                .url(channelDTO.getUrl())
                .owner(user)
                .tagList(channelDTO.getTagList())
                .description(channelDTO.getDescription())
                .password(channelDTO.getPassword() != null? encoder.encode(channelDTO.getPassword()) : null)
                .isLocked(channelDTO.getPassword() != null && !channelDTO.getPassword().isEmpty())
                .maxUsers(channelDTO.getMaxUsers())
                .allowOthersToManageSongs(channelDTO.getAllowOthersToManageSongs())
                .allowOthersToControlPlayback(channelDTO.getAllowOthersToControlPlayback())
                .members(Map.of(user.getId(), LocalTime.now()))
                .addedBy(user.getId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public ChannelJoinPageDTO toChannelJoinPageDTO(Channel channel) {
        return ChannelJoinPageDTO.builder()
                .id(channel.getId())
                .name(channel.getName())
                .url(channel.getUrl())
                .tagList(channel.getTagList())
                .description(channel.getDescription())
                .password(channel.getPassword())
                .isLocked(channel.getIsLocked())
                .maxUsers(channel.getMaxUsers())
                .currentUsers(channel.getMembers().size())
                .ownerDisplayName(channel.getOwner().getDisplayName())
                .ownerAvatar(channel.getOwner().getAvatar())
                .build();
    }

    public Mono<ChannelInfoDTO> toChannelInfoDTO(Channel channel) {
        Mono<List<SongDTO>> songDTOListMono = Flux.fromIterable(channel.getSongs())
                .flatMap(songMapper::toSongDTO)
                .collectList();

        Mono<List<MemberInfoDTO>> membersMono = userMapper.toMembersMap(channel.getMembers(), channel.getOwner().getId());

        return Mono.zip(songDTOListMono, membersMono)
                .map(tuple -> {
                    List<SongDTO> songs = tuple.getT1();
                    List<MemberInfoDTO> members = tuple.getT2();

                    return ChannelInfoDTO.builder()
                            .id(channel.getId())
                            .name(channel.getName())
                            .url(channel.getUrl())
                            .tagList(channel.getTagList())
                            .description(channel.getDescription())
                            .isLocked(channel.getIsLocked())
                            .maxUsers(channel.getMaxUsers())
                            .currentUsers(channel.getMembers().size())
                            .allowOthersToManageSongs(channel.getAllowOthersToManageSongs())
                            .allowOthersToControlPlayback(channel.getAllowOthersToControlPlayback())
                            .songs(songs)
                            .members(members)
                            .build();
                });
    }

}
