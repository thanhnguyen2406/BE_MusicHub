package musichub.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "votes")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vote extends BaseEntity {
    @Id
    String id;
    
    @NotNull
    String songId;
    
    @Builder.Default
    int upVoteCount = 0;

    @Builder.Default
    int downVoteCount = 0;

    @Builder.Default
    Map<String, Boolean> userVotes = new HashMap<>(); // userId -> isUpvote (true for upvote, false for downvote)
    
    @Builder.Default
    Map<String, LocalDateTime> voteTimestamps = new HashMap<>(); // userId -> timestamp of vote
}
