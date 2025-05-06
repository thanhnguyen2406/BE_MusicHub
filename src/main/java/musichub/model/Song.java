package musichub.model;

import musichub.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "songs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Song extends BaseEntity {
    @Id
    String id;

    @NotNull
    String channelId;

    @NotNull
    String title;

    @NotNull
    String artist;

    @NotNull
    String url;

    @NotNull
    String moodTag;

    @NotNull
    String thumbnail;

    @NotNull
    Integer duration;

    @NotNull
    Status status;

    @Builder.Default
    List<String> votes = new ArrayList<>();
    
    @Builder.Default
    Integer totalUpVotes = 0;
    
    @Builder.Default
    Integer totalDownVotes = 0;
}
