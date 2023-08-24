package searchengine.model;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "`index`")
public class Indexes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "page_id", nullable = false)
    private int page_id;

    @Column(name = "lemma_id", nullable = false)
    private int lemma_id;

    @Column(name = "`rank`", nullable = false)
    private float rank;
}
