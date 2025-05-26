package musichub.repository;

import musichub.model.Channel;
import musichub.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ChannelRepository extends ReactiveMongoRepository<Channel, String> {
    Mono<Channel> findByUrl(String url);
    Mono<Channel> findByOwner(User owner);
}