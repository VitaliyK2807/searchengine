package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemmas;

@Repository
public interface LemmasRepository extends JpaRepository<Lemmas, Integer> {
}
