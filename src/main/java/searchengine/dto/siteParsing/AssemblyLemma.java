package searchengine.dto.siteParsing;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class AssemblyLemma {
    private String lemma;
    private int rank;
    private int page_id;

    public AssemblyLemma(String lemma, int rank) {
        this.lemma = lemma;
        this.rank = rank;
    }

    public AssemblyLemma() {
    }
}
