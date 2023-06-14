package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lemma")
public class Lemmas {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String site;
//    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JoinColumn(name = "sites_id", nullable = false)
//    private Sites siteId;

    @Column(nullable = false, columnDefinition="VARCHAR(255)")
    private String lemma;

    @Column(nullable = false)
    private int frequency;
}
