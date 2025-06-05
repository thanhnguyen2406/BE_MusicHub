package musichub.mapper;

import lombok.RequiredArgsConstructor;
import musichub.dto.SongDTO.SongDTO;
import musichub.exception.AppException;
import musichub.exception.ErrorCode;
import musichub.model.Song;
import musichub.repository.SongRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SongMapper {
    private final SongRepository songRepository;

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

    public Mono<SongDTO> toSongDTO(String songId) {
        return songRepository.findById(songId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.SONG_NOT_FOUND)))
                .map(song ->
                        SongDTO.builder()
                                .id(song.getId())
                                .title(song.getTitle())
                                .artist(song.getArtist())
                                .url(song.getUrl())
                                .moodTag(song.getMoodTag())
                                .thumbnail(song.getThumbnail())
                                .duration(song.getDuration())
                                .status(song.getStatus())
                                .totalUpVotes(song.getVote().getUpVoteCount())
                                .totalDownVotes(song.getVote().getDownVoteCount())
                                .build());
    }
}