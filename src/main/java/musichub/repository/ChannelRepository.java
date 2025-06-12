package musichub.repository;

import musichub.model.Channel;
import musichub.model.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ChannelRepository extends ReactiveMongoRepository<Channel, String>, ChannelRepositoryCustom {
    Mono<Channel> findByUrl(String url);

    @Query("{ 'members.?0' : { $exists: true } }")
    Mono<Channel> findByMemberId(String userId);
}