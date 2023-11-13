package searchengine.model;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "site")
public class Sites {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pages> page = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Lemmas> lemmas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    private Status status;

    @Column(nullable = false)
    private LocalDateTime statusTime;

    private String lastError;

    @Column(nullable = false, columnDefinition="VARCHAR(255)")
    private String url;

    @Column(nullable = false)
    private String name;

}
