package MusicHub.model;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vote {
    @NotNull
    int upVote = 0;

    @NotNull
    int downVote = 0;

    @NotNull
    Map<String, Integer> userVote = new HashMap<>();
}
