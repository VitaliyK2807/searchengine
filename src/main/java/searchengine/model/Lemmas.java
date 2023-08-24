package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "`lemma`")
public class Lemmas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false, referencedColumnName = "id")
    private Sites siteId;

    @Column(name = "`lemma`", nullable = false, columnDefinition="VARCHAR(255)")
    private String lemma;

    @Column(nullable = false)
    private int frequency;
}
