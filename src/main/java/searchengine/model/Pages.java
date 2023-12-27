package searchengine.model;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "page", indexes = @javax.persistence.Index(columnList = "path"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"site_id", "path"}))
public class Pages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id", nullable = false)
    private Sites siteId;

    @Column(name = "path", columnDefinition = "VARCHAR(511)", length = 511)
    private String path;

    @Column(name = "code", nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

//    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
//    private Set<Indexes> indexes = new HashSet<>();

}
