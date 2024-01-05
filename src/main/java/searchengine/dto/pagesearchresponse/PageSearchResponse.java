package searchengine.dto.pagesearchresponse;

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
    private int count;
    private List<DataSearch> data;

    public PageSearchResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public PageSearchResponse(boolean result, int count, List<DataSearch> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }
}
