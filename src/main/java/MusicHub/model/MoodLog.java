package MusicHub.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public class MoodLog {
    @Id
    String id;

    @NotNull
    String addedByUserId;

    String imageUrl;

    String moodTag;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Ho_Chi_Minh")
    LocalDateTime createdAt;
}
