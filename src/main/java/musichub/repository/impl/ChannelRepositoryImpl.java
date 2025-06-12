package musichub.repository.impl;

import lombok.RequiredArgsConstructor;
import musichub.model.Channel;
import musichub.repository.ChannelRepositoryCustom;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class ChannelRepositoryImpl implements ChannelRepositoryCustom {
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Flux<Channel> findAllWithPage(int page, int size) {
        Query query = new Query()
                .skip((long) page * size)
                .limit(size);
        return mongoTemplate.find(query, Channel.class);
    }
}

