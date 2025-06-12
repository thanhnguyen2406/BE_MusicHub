package musichub.controller;

import lombok.RequiredArgsConstructor;
import musichub.dto.RequestRsocket;
import musichub.model.Song;
import musichub.service.SongService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class SongServerController {
    private final SongService songService;

    public static final String CREATE = "song.add";
    public static final String DELETE = "song.delete";
    public static final String VOTE = "song.vote";

    @MessageMapping(CREATE)
    public Mono<Song> addSongServer(RequestRsocket requestRsocket) {
        return songService.addSongServer(requestRsocket);
    }

    @MessageMapping(DELETE)
    public Mono<Void> deleteSongServer(RequestRsocket requestRsocket) {
        return songService.deleteSongServer(requestRsocket);
    }

    @MessageMapping(VOTE)
    public Mono<Song> voteSongServer(RequestRsocket requestRsocket) {
        return songService.voteSongServer(requestRsocket);
    }
}
