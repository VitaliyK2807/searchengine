package searchengine.dto.indexingSites;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class IndexingStopResponse {
    private boolean result;
    private String error;

    public IndexingStopResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public IndexingStopResponse(boolean result) {
        this.result = result;
    }
}
