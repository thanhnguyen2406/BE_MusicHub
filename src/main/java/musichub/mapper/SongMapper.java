package musichub.mapper;

import musichub.dto.SongDTO.SongDTO;
import musichub.model.Song;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SongMapper {
    public Song toSong(SongDTO songDTO, String channelId, String userId) {
        return Song.builder()
                .title(songDTO.getTitle())
                .artist(songDTO.getArtist())
                .url(songDTO.getUrl())
                .moodTag(songDTO.getMoodTag())
                .thumbnail(songDTO.getThumbnail())
                .duration(songDTO.getDuration())
                .channelId(channelId)
                .addedBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}