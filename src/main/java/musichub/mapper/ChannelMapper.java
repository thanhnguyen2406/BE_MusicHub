package musichub.mapper;

import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.model.Channel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Component
public class ChannelMapper {
    public Channel toChannel(ChannelDTO channelDTO, String userId) {
        return Channel.builder()
                .name(channelDTO.getName())
                .url(channelDTO.getUrl())
                .tagList(channelDTO.getTagList())
                .description(channelDTO.getDescription())
                .password(channelDTO.getPassword())
                .isLocked(channelDTO.getPassword() != null && !channelDTO.getPassword().isEmpty())
                .maxUsers(channelDTO.getMaxUsers())
                .allowOthersToManageSongs(channelDTO.getAllowOthersToManageSongs())
                .allowOthersToControlPlayback(channelDTO.getAllowOthersToControlPlayback())
                .members(Map.of(userId, LocalTime.now()))
                .addedBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
