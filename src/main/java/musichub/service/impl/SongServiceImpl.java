package musichub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import musichub.common.ChannelPermissionUtil;
import musichub.common.ResponseUtil;
import musichub.controller.SongServerController;
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
import musichub.service.SongService;
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
public class SongServiceImpl implements SongService {
    SongRepository songRepository;
    ChannelRepository channelRepository;
    SongMapper songMapper;
    RSocketRequester rSocketRequester;

    private static final boolean LIKED = true;
    private static final boolean DISLIKED = false;
    private static final String USER_ID = "userId";
    private static final String SONG_ID = "songId";
    private static final String SONG_DTO = "songDTO";
    private static final String VOTE_TYPE = "voteType";
    private static final String LIKE = "like";

    //#region Server Service

    @Override
    public Mono<Song> addSongServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs(ChannelServiceImpl.CHANNEL_ID, String.class);
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);
        SongDTO songDTO = requestRsocket.getPayloadAs(SONG_DTO, SongDTO.class);

        return getChannelIfExists(channelId)
                .flatMap(channel -> verifyOwnerPermission(channel, userId)
                        .then(saveSongAndUpdateChannel(songDTO, channel, userId))
                );
    }

    private Mono<Void> verifyOwnerPermission(Channel channel, String userId) {
        return ChannelPermissionUtil.requireOwner(channel, userId);
    }

    private Mono<Song> saveSongAndUpdateChannel(SongDTO songDTO, Channel channel, String userId) {
        Song song = songMapper.toSong(songDTO, channel.getId(), userId);

        return songRepository.save(song)
                .flatMap(savedSong -> {
                    channel.getSongs().add(savedSong.getId());
                    return sortChannelSongs(channel)
                            .then(channelRepository.save(channel))
                            .thenReturn(savedSong);
                });
    }

    @Override
    public Mono<Void> deleteSongServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs(ChannelServiceImpl.CHANNEL_ID, String.class);
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);
        String songId = requestRsocket.getPayloadAs(SONG_ID, String.class);

        return getChannelIfExists(channelId)
                .flatMap(channel -> ChannelPermissionUtil.requireAllowManageSongs(channel, userId)
                        .then(deleteSongFromChannel(channel, songId)));
    }

    private Mono<Void> deleteSongFromChannel(Channel channel, String songId) {
        return songRepository.findById(songId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.SONG_NOT_FOUND)))
                .flatMap(song -> ChannelPermissionUtil.requireSongContainedInChannel(channel, songId)
                        .then(songRepository.delete(song))
                        .then(channelRepository.save(removeSongFromChannel(channel, songId)))
                        .flatMap(this::sortChannelSongs)
                        .then());
    }

    private Channel removeSongFromChannel(Channel channel, String songId) {
        channel.getSongs().remove(songId);
        return channel;
    }

    private Mono<Channel> getChannelIfExists(String channelId) {
        return channelRepository.findById(channelId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.CHANNEL_NOT_FOUND)));
    }

    @Override
    public Mono<Song> voteSongServer(RequestRsocket requestRsocket) {
        String channelId = requestRsocket.getPayloadAs(ChannelServiceImpl.CHANNEL_ID, String.class);
        String userId = requestRsocket.getPayloadAs(USER_ID, String.class);
        String songId = requestRsocket.getPayloadAs(SONG_ID, String.class);
        String voteType = requestRsocket.getPayloadAs(VOTE_TYPE, String.class);

        return getChannelIfExists(channelId)
                .flatMap(channel ->
                        ChannelPermissionUtil.requireMember(channel, userId)
                                .then(ChannelPermissionUtil.requireSongContainedInChannel(channel, songId))
                                .then(getVoteMono(voteType, songId, userId))
                                .flatMap(votedSong ->
                                        sortChannelSongs(channel).thenReturn(votedSong)
                                )
                );
    }

    private Mono<Song> getVoteMono(String voteType, String songId, String userId) {
        return switch (voteType) {
            case "like" -> likeSong(songId, userId);
            case "dislike" -> dislikeSong(songId, userId);
            default -> Mono.error(new AppException(ErrorCode.INVALID_VOTE_TYPE));
        };
    }
    //#endregion

    //#region Client Service

    @Override
    public Mono<ResponseAPI<Void>> addSong(String channelId, SongDTO songDTO, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(ChannelServiceImpl.CHANNEL_ID, channelId);
        requestRsocket.setPayload(SONG_DTO, songDTO);
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(SongServerController.CREATE)
                .data(requestRsocket)
                .retrieveMono(Song.class)
                .map(song -> ResponseUtil.success("Song added successfully"));
    }

    @Override
    public Mono<ResponseAPI<Void>> deleteSong(String channelId, String songId, String userId) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(ChannelServiceImpl.CHANNEL_ID, channelId);
        requestRsocket.setPayload(SONG_ID, songId);
        requestRsocket.setPayload(USER_ID, userId);

        return rSocketRequester
                .route(SongServerController.DELETE)
                .data(requestRsocket)
                .retrieveMono(Void.class)
                .then(Mono.fromCallable(() -> ResponseUtil.success("Song deleted successfully")));
    }

    @Override
    public Mono<ResponseAPI<VoteSongDTO>> voteSong(String channelId, String songId, String userId, String voteType) {
        RequestRsocket requestRsocket = new RequestRsocket();
        requestRsocket.setPayload(ChannelServiceImpl.CHANNEL_ID, channelId);
        requestRsocket.setPayload(SONG_ID, songId);
        requestRsocket.setPayload(USER_ID, userId);
        requestRsocket.setPayload(VOTE_TYPE, voteType);

        return rSocketRequester
                .route(SongServerController.VOTE)
                .data(requestRsocket)
                .retrieveMono(Void.class)
                .then(Mono.fromCallable(() -> ResponseUtil.success("Song voted successfully")));
    }

    public Mono<Channel> sortChannelSongs(Channel channel) {
        return songRepository.findAllById(channel.getSongs())
                .collectList()
                .map(this::sortSongs)
                .map(sortedSongs -> updateChannelWithSortedSongs(channel, sortedSongs))
                .flatMap(channelRepository::save);
    }

    private List<Song> sortSongs(List<Song> songs) {
        return songs.stream()
                .sorted(
                        Comparator
                                .comparing(Song::getStatus, Comparator.comparingInt(this::getStatusPriority))
                                .thenComparing(song -> song.getStatus() == Status.WAITING
                                        ? song.getVote().getUpVoteCount() - song.getVote().getDownVoteCount()
                                        : 0)
                )
                .toList();
    }

    private Channel updateChannelWithSortedSongs(Channel channel, List<Song> sortedSongs) {
        List<String> sortedIds = sortedSongs.stream().map(Song::getId).toList();
        channel.setSongs(sortedIds);
        return channel;
    }

    private int getStatusPriority(Status status) {
        return switch (status) {
            case WAITING -> 0;
            case FINISHED -> 1;
            case PLAYING -> 2;
        };
    }

    public Mono<Song> likeSong(String songId, String userId) {
        return processVote(songId, userId, true);
    }

    public Mono<Song> dislikeSong(String songId, String userId) {
        return processVote(songId, userId, false);
    }

    private Mono<Song> processVote(String songId, String userId, boolean isLike) {
        return songRepository.findById(songId)
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.SONG_NOT_FOUND)))
                .flatMap(song -> {
                    Vote vote = song.getVote();
                    Map<String, Boolean> userVotes = vote.getUserVotes();
                    Map<String, LocalDateTime> voteTimestamps = vote.getVoteTimestamps();

                    boolean hasVoted = userVotes.containsKey(userId);
                    boolean previousVote = userVotes.getOrDefault(userId, !isLike);

                    if (hasVoted && previousVote == isLike) {
                        userVotes.remove(userId);
                        voteTimestamps.remove(userId);
                        updateVoteCount(vote, isLike, -1);
                    } else {
                        userVotes.put(userId, isLike);
                        voteTimestamps.put(userId, LocalDateTime.now());
                        if (hasVoted) updateVoteCount(vote, !isLike, -1);
                        updateVoteCount(vote, isLike, +1);
                    }

                    vote.setUserVotes(userVotes);
                    vote.setVoteTimestamps(voteTimestamps);
                    song.setVote(vote);
                    return songRepository.save(song);
                });
    }

    private void updateVoteCount(Vote vote, boolean isLike, int delta) {
        if (isLike) {
            vote.setUpVoteCount(Math.max(0, vote.getUpVoteCount() + delta));
        } else {
            vote.setDownVoteCount(Math.max(0, vote.getDownVoteCount() + delta));
        }
    }
    //#endregion
}