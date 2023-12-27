package searchengine.model;

import lombok.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Entity
@Table(name = "lemma", uniqueConstraints = @UniqueConstraint(columnNames = {"site_id", "lemma"}))

public class Lemmas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Sites siteId;

    @Column(name = "`lemma`", nullable = false, columnDefinition = "VARCHAR(255)")
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private int frequency;

//    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
//    @ToString.Exclude
//    private Set<Indexes> indexes = new HashSet<>();

}
