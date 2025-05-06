package musichub.mapper;

import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.model.Channel;
import org.springframework.stereotype.Component;

@Component
public class ChannelMapper {
    public Channel toChannel(ChannelDTO channelDTO) {
        return Channel.builder()
                .name(channelDTO.getName())
                .url(channelDTO.getUrl())
                .tagList(channelDTO.getTagList())
                .description(channelDTO.getDescription())
                .password(channelDTO.getPassword())
                .isLocked(channelDTO.getPassword() != null && !channelDTO.getPassword().isEmpty())
                .maxUsers(channelDTO.getMaxUsers())
                .allowOthersToAddSongs(channelDTO.getAllowOthersToAddSongs())
                .allowOthersToControlPlayback(channelDTO.getAllowOthersToControlPlayback())
                .build();
    }
}
