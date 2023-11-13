package searchengine.dto.pageSearch;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class PageSearchResponse {
    private boolean result;
    private String error;

    public PageSearchResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public PageSearchResponse(boolean result) {
        this.result = result;
    }
}
