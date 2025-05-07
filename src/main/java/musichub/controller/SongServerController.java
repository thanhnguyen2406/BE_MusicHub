package musichub.controller;

import lombok.RequiredArgsConstructor;
import musichub.dto.RequestRsocket;
import musichub.model.Song;
import musichub.service.interf.ISongService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class SongServerController {
    private final ISongService songService;

    @MessageMapping("song.add")
    public Mono<Song> addSongServer(RequestRsocket requestRsocket) {
        return songService.addSongServer(requestRsocket);
    }

    @MessageMapping("song.delete")
    public Mono<Void> deleteSongServer(RequestRsocket requestRsocket) {
        return songService.deleteSongServer(requestRsocket);
    }

    @MessageMapping("song.vote")
    public Mono<Song> voteSongServer(RequestRsocket requestRsocket) {
        return songService.voteSongServer(requestRsocket);
    }
}
