package searchengine.dto.indexingsites;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class IndexingSitesResponse {

    private boolean result;
    private String error;

    public IndexingSitesResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public IndexingSitesResponse(boolean result) {
        this.result = result;
    }

}
