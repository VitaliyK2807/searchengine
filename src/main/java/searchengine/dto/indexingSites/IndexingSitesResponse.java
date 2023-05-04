package searchengine.dto.indexingSites;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.config.Site;
import searchengine.config.SitesList;

import java.util.List;

@Data
@Getter
@Setter
public class IndexingSitesResponse {

    private boolean result;
    private int countSitesIndexing;
    private int countPagesIndexing;
    private int countErrors;


}
