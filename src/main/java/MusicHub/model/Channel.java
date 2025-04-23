package MusicHub.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "channels")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Channel {
    @Id
    @JsonProperty("id")
    String id;

    @NotNull
    @JsonProperty("name")
    String name;

    @NotNull
    @JsonProperty("url")
    String url;

    @NotNull
    @JsonProperty("tagList")
    List<String> tagList;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Ho_Chi_Minh")
    @JsonProperty("createdAt")
    LocalDateTime createdAt;

    @NotNull
    @JsonProperty("members")
    Map<String, LocalTime> members = new HashMap<>();

    @NotNull
    @JsonProperty("songs")
    List<String> songs = List.of();

    @JsonProperty("description")
    String description;
    @JsonProperty("password")
    String password;

    /* For enhancement
    @Field("votes")
    Vote votes;*/
}
