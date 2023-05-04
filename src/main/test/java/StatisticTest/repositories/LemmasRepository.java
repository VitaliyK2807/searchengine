package StatisticTest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemmas;

@Repository
public interface LemmasRepository extends JpaRepository<Lemmas, Integer> {
    @Query("select count(l) from Lemmas l where l.siteId = ?1")
    int countRecordsById(int siteId);


}
