package musichub.service.interf;

import musichub.dto.RequestRsocket;
import musichub.dto.ResponseAPI;
import musichub.dto.SongDTO.SongDTO;
import musichub.dto.SongDTO.VoteSongDTO;
import musichub.model.Song;
import reactor.core.publisher.Mono;

public interface ISongService {
    //Server
    Mono<Song> addSongServer(RequestRsocket requestRsocket);
    Mono<Void> deleteSongServer(RequestRsocket requestRsocket);
    Mono<Song> voteSongServer(RequestRsocket requestRsocket);

    //Client
    Mono<ResponseAPI<Void>> addSong(String channelId, SongDTO songDTO, String userId);
    Mono<ResponseAPI<Void>> deleteSong(String channelId, String songId, String userId);
    Mono<ResponseAPI<VoteSongDTO>> voteSong(String channelId, String songId, String userId, String voteType);
}
