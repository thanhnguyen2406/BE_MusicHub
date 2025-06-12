package musichub.model;

import lombok.experimental.SuperBuilder;
import musichub.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "songs")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

    @Builder.Default
    Status status = Status.WAITING;

    @Builder.Default
    Vote vote = new Vote();
}