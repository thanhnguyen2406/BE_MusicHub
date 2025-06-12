package musichub.repository;

import musichub.model.Channel;
import reactor.core.publisher.Flux;

public interface ChannelRepositoryCustom {
    Flux<Channel> findAllWithPage(int page, int size);
}