package MusicHub.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Channel {
    @Id
    String id;

    @NotNull
    String name;

    @NotNull
    String url;

    @NotNull
    List<String> tagList;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Ho_Chi_Minh")
    LocalDateTime createdAt;

    @NotNull
    Map<String, LocalTime> members = new HashMap<>();

    @NotNull
    List<String> songs = List.of();

    String description;
    String password;

    /* For enhancement
    @Field("votes")
    Vote votes;*/
}
