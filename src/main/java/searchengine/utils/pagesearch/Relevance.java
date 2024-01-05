package searchengine.utils.pagesearch;

import lombok.*;
import searchengine.model.Sites;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Relevance implements Comparable<Relevance>{

    private Sites site;
    private Integer pageId;
    private Double absolutesRelevance;
    private Double relativeRelevance;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relevance relevance = (Relevance) o;
        return Objects.equals(site, relevance.site) && Objects.equals(pageId, relevance.pageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, pageId);
    }

    @Override
    public int compareTo(Relevance o) {
        return relativeRelevance.compareTo(o.relativeRelevance);
    }
}
