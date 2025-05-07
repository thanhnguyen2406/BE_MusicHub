package musichub.controller;

import lombok.RequiredArgsConstructor;
import musichub.dto.ChannelDTO.ChannelDTO;
import musichub.dto.ResponseAPI;
import musichub.dto.SongDTO.SongDTO;
import musichub.dto.SongDTO.VoteSongDTO;
import musichub.service.interf.ISongService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/channels/{channelId}/songs")
public class SongClientController {
    private final ISongService songService;

    @PostMapping
    public Mono<ResponseAPI<Void>> addSong(@PathVariable String channelId, @RequestBody SongDTO songDTO, Principal principal) {
        return songService.addSong(channelId, songDTO, principal.getName());
    }

    @DeleteMapping("/{songId}")
    public Mono<ResponseAPI<Void>> deleteSong(@PathVariable String channelId, @PathVariable String songId, Principal principal) {
        return songService.deleteSong(channelId, songId, principal.getName());
    }

    @PostMapping("/{songId}/like")
    public Mono<ResponseAPI<VoteSongDTO>> likeSong(@PathVariable String channelId, @PathVariable String songId, Principal principal) {
        return songService.voteSong(channelId, songId, principal.getName(), "like");
    }

    @PostMapping("/{songId}/dislike")
    public Mono<ResponseAPI<VoteSongDTO>> dislikeSong(@PathVariable String channelId, @PathVariable String songId, Principal principal) {
        return songService.voteSong(channelId, songId, principal.getName(), "dislike");
    }
}
