package searchengine.dto.indexPageResponse;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class IndexPageResponse {

    private boolean result;
    private String error;

    public IndexPageResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public IndexPageResponse(boolean result) {
        this.result = result;
    }
}
