package MusicHub.model;

import MusicHub.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Song {
    @Id
    String id;

    @NotNull
    String channelId;

    @NotNull
    String addedByUserId;

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

    @Field("votes")
    Vote votes;
}
