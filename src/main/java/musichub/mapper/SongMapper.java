package musichub.mapper;

import musichub.dto.SongDTO.SongDTO;
import musichub.model.Song;
import org.springframework.stereotype.Component;

@Component
public class SongMapper {
    public Song toSong(SongDTO songDTO) {
        return Song.builder()
                .title(songDTO.getTitle())
                .artist(songDTO.getArtist())
                .url(songDTO.getUrl())
                .moodTag(songDTO.getMoodTag())
                .thumbnail(songDTO.getThumbnail())
                .duration(songDTO.getDuration())
                .build();
    }
}