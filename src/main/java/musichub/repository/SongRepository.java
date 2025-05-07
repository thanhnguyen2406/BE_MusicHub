package musichub.repository;

import musichub.model.Song;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SongRepository extends ReactiveMongoRepository<Song, String> {
}
