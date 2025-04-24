package MusicHub.mapper;

import MusicHub.dto.ChannelDTO.ChannelDTO;
import MusicHub.model.Channel;
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
                .build();
    }
}
