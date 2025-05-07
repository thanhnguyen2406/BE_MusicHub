package musichub.service.implement;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import musichub.dto.RequestRsocket;
import musichub.dto.ResponseAPI;
import musichub.dto.SongDTO.SongDTO;
import musichub.dto.SongDTO.VoteSongDTO;
import musichub.enums.Status;
import musichub.exception.AppException;
import musichub.exception.ErrorCode;
import musichub.mapper.SongMapper;
import musichub.model.Channel;
import musichub.model.Song;
import musichub.model.Vote;
import musichub.repository.ChannelRepository;
import musichub.repository.SongRepository;
import musichub.service.interf.ISongService;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class SongService implements ISongService {
    SongRepository songRepository;
    ChannelRepository channelRepository;
    SongMapper songMapper;
    RSocketRequester rSocketRequester;

    static boolean LIKED = true;
    static boolean DISLIKED = false;

    //Server

    @Override
    public Mono<Song> addSongServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs("channelId", String.class);
        String userId = requestRsocket.getPayloadAs("userId", String.class);
        SongDTO songDTO = requestRsocket.getPayloadAs("songDTO", SongDTO.class);

        return channelRepository.findById(requestRsocket.getPayloadAs("channelId", String.class))
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (channel.getAddedBy().equals(userId) || channel.getAllowOthersToManageSongs()) {
                        Song song = songMapper.toSong(songDTO, channelId, userId);

                        return songRepository.save(song)
                                .flatMap(savedSong -> {
                                    channel.getSongs().add(savedSong.getId());
                                    return sortChannelSongs(channel)
                                            .thenReturn(savedSong);
                                });
                    } else {
                        return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_CHANNEL_OWNER));
                    }
                });
    }

    @Override
    public Mono<Void> deleteSongServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs("channelId", String.class);
        String userId = requestRsocket.getPayloadAs("userId", String.class);
        String songId = requestRsocket.getPayloadAs("songId", String.class);

        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (channel.getAddedBy().equals(userId) || channel.getAllowOthersToManageSongs()) {
                        return songRepository.deleteById(songId);
                    } else {
                        return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_CHANNEL_OWNER));
                    }
                });
    }

    @Override
    public Mono<Song> voteSongServer(RequestRsocket requestRsocket) {
        String userId = requestRsocket.getPayloadAs("userId", String.class);
        String songId = requestRsocket.getPayloadAs("songId", String.class);
        String voteType = requestRsocket.getPayloadAs("voteType", String.class);

        return channelRepository.findById(requestRsocket.getPayloadAs("channelId", String.class))
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)))
                .flatMap(channel -> {
                    if (!channel.getMembers().containsKey(userId)) {
                        return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_CHANNEL_MEMBER));
                    }
                    if (!channel.getSongs().contains(songId)) {
                        return Mono.error(new AppException(ErrorCode.SONG_NOT_FOUND));
                    }

                    Mono<Song> voteMono = voteType.equals("like")
                            ? likeSong(songId, userId)
                            : dislikeSong(songId, userId);

                    return voteMono.flatMap(votedSong ->
                            sortChannelSongs(channel).thenReturn(votedSong)
                    );
                });
    }


    //Client

    @Override
    public Mono<ResponseAPI<Void>> addSong(String channelId, SongDTO songDTO, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("songDTO", songDTO);
        requestRsocket.setPayload("userId", userId);

        return rSocketRequester
                .route("song.add")
                .data(requestRsocket)
                .retrieveMono(Song.class)
                .map(channel -> ResponseAPI.<Void>builder()
                        .code(200)
                        .message("Song created successfully")
                        .build())
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Failed to create song: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> deleteSong(String channelId, String songId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("songId", songId);
        requestRsocket.setPayload("userId", userId);

        return rSocketRequester
                .route("song.delete")
                .data(requestRsocket)
                .retrieveMono(Void.class)
                .then(Mono.fromCallable(() -> ResponseAPI.<Void>builder()
                        .code(200)
                        .message("Song deleted successfully")
                        .build()))
                .onErrorResume(e -> Mono.just(ResponseAPI.<Void>builder()
                        .code(500)
                        .message("Failed to create song: " + e.getMessage())
                        .build()));
    }

    @Override
    public Mono<ResponseAPI<VoteSongDTO>> voteSong(String channelId, String songId, String userId, String voteType) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload("channelId", channelId);
        requestRsocket.setPayload("songId", songId);
        requestRsocket.setPayload("userId", userId);
        requestRsocket.setPayload("voteType", voteType);

        return rSocketRequester
                .route("song.vote")
                .data(requestRsocket)
                .retrieveMono(Void.class)
                .then(Mono.fromCallable(() -> ResponseAPI.<VoteSongDTO>builder()
                        .code(200)
                        .message("Song voted successfully")
                        .build()))
                .onErrorResume(e -> Mono.just(ResponseAPI.<VoteSongDTO>builder()
                        .code(500)
                        .message("Failed to vote song: " + e.getMessage())
                        .build()));
    }

    public Mono<Channel> sortChannelSongs(Channel channel) {
        return songRepository.findAllById(channel.getSongs())
                .collectList()
                .map(songs -> {
                    List<Song> sorted = songs.stream()
                            .sorted(Comparator
                                    .comparing(Song::getStatus, Comparator.comparingInt(this::getStatusPriority))
                                    .thenComparing(song -> {
                                        if (song.getStatus() == Status.WAITING) {
                                            return song.getVote().getUpVoteCount() - song.getVote().getDownVoteCount();
                                        }
                                        return 0;
                                    })
                            )
                            .toList();

                    List<String> sortedIds = sorted.stream().map(Song::getId).toList();
                    channel.setSongs(sortedIds);
                    return channel;
                })
                .flatMap(channelRepository::save);
    }

    private int getStatusPriority(Status status) {
        return switch (status) {
            case WAITING -> 0;
            case FINISHED -> 1;
            case PLAYING -> 2;
        };
    }

    public Mono<Song> likeSong(String songId, String userId) {
        return songRepository.findById(songId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.SONG_NOT_FOUND)))
                .flatMap(song -> {
                    Vote vote = song.getVote();
                    Map<String, Boolean> userVotes = vote.getUserVotes();
                    Map<String, LocalDateTime> voteTimestamps = vote.getVoteTimestamps();

                    if (userVotes.containsKey(userId)) {
                        boolean previousVote = userVotes.get(userId);
                        if (previousVote) {
                            // Unlike (remove like)
                            userVotes.remove(userId);
                            voteTimestamps.remove(userId);
                            vote.setUpVoteCount(Math.max(0, vote.getUpVoteCount() - 1));
                        } else {
                            // Change from dislike to like
                            userVotes.put(userId, LIKED);
                            voteTimestamps.put(userId, LocalDateTime.now());
                            vote.setDownVoteCount(Math.max(0, vote.getDownVoteCount() - 1));
                            vote.setUpVoteCount(vote.getUpVoteCount() + 1);
                        }
                    } else {
                        // First time like
                        userVotes.put(userId, LIKED);
                        voteTimestamps.put(userId, LocalDateTime.now());
                        vote.setUpVoteCount(vote.getUpVoteCount() + 1);
                    }
                    vote.setUserVotes(userVotes);
                    vote.setVoteTimestamps(voteTimestamps);
                    song.setVote(vote);
                    return songRepository.save(song);
                });
    }

    public Mono<Song> dislikeSong(String songId, String userId) {
        return songRepository.findById(songId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.SONG_NOT_FOUND)))
                .flatMap(song -> {
                    Vote vote = song.getVote();
                    Map<String, Boolean> userVotes = vote.getUserVotes();
                    Map<String, LocalDateTime> voteTimestamps = vote.getVoteTimestamps();

                    if (userVotes.containsKey(userId)) {
                        boolean previousVote = userVotes.get(userId);
                        if (!previousVote) {
                            // remove dislike
                            userVotes.remove(userId);
                            voteTimestamps.remove(userId);
                            vote.setDownVoteCount(Math.max(0, vote.getDownVoteCount() - 1));
                        } else {
                            // Change from like to dislike
                            userVotes.put(userId, DISLIKED);
                            voteTimestamps.put(userId, LocalDateTime.now());
                            vote.setUpVoteCount(Math.max(0, vote.getUpVoteCount() - 1));
                            vote.setDownVoteCount(vote.getDownVoteCount() + 1);
                        }
                    } else {
                        // First time dislike
                        userVotes.put(userId, DISLIKED);
                        voteTimestamps.put(userId, LocalDateTime.now());
                        vote.setDownVoteCount(vote.getDownVoteCount() + 1);
                    }
                    vote.setUserVotes(userVotes);
                    vote.setVoteTimestamps(voteTimestamps);
                    song.setVote(vote);
                    return songRepository.save(song);
                });
    }
}