package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Indexes;

@Repository
public interface IndexesRepository extends JpaRepository<Indexes, Integer> {
}
