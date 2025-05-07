package musichub.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "channels")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Channel extends BaseEntity{
    @Id
    @JsonProperty("id")
    String id;

    @NotNull
    @JsonProperty("name")
    String name;

    @NotNull
    @JsonProperty("url")
    @Indexed(unique = true)
    String url;

    @NotNull
    @JsonProperty("tagList")
    @Builder.Default
    List<String> tagList = new ArrayList<>();

    @NotNull
    @JsonProperty("members")
    @Builder.Default
    Map<String, LocalTime> members = new HashMap<>();

    @NotNull
    @JsonProperty("songs")
    @Builder.Default
    List<String> songs = new ArrayList<>();

    @JsonProperty("description")
    String description;

    @JsonProperty("password")
    String password;
    
    @JsonProperty("isLocked")
    @Builder.Default
    Boolean isLocked = false;
    
    @JsonProperty("maxUsers")
    Integer maxUsers;
    
    @JsonProperty("allowOthersToAddSongs")
    @Builder.Default
    Boolean allowOthersToManageSongs = false;
    
    @JsonProperty("allowOthersToControlPlayback")
    @Builder.Default
    Boolean allowOthersToControlPlayback = false;
    
    /*@DBRef
    User owner;*/

    /* For enhancement
    @Field("votes")
    Vote votes;*/
}
