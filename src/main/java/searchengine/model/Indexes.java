package searchengine.model;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Entity
@Transactional
@Table(name = "`index`", uniqueConstraints = @UniqueConstraint(columnNames = {"page_id", "lemma_id"}))
public class Indexes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Pages page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemmas lemma;

    @Column(name = "`rank`", nullable = false)
    private float rank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Indexes index = (Indexes) o;
        return Objects.equals(page, index.page) && Objects.equals(lemma, index.lemma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, lemma);
    }
}
