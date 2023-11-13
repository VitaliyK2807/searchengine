package searchengine.model;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "page", indexes = @javax.persistence.Index(columnList = "path"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"site_id", "path"}))
public class Pages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Sites site;

    @Column(name = "path", columnDefinition = "VARCHAR(511)", length = 511)
    private String path;

    @Column(name = "code", nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Indexes> indexes = new ArrayList<>();

}
