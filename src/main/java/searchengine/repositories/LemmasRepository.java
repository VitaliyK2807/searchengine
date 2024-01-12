package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemmas;
import searchengine.model.Sites;
import java.util.List;
import java.util.Optional;

@Repository
public interface LemmasRepository extends JpaRepository<Lemmas, Integer> {
    @Transactional
    @Modifying
    @Query("update Lemmas l set l.frequency = ?1 where l.id = ?2")
    void updateLemma(int frequency, int id);
    @Query("select l from Lemmas l where l.lemma = ?1 and l.siteId = ?2")
    Optional<Lemmas> findByLemmaAndSiteId(String lemma, Sites siteId);

    @Query("select l from Lemmas l where l.lemma = ?1")
    List<Lemmas> findByLemma(String lemma);

    @Query("select count(*) from Lemmas l")
    int getTotalLemmas();

    @Query("select count(*) from Lemmas l where l.siteId = ?1")
    int getTotalSitesWithLemmas (Sites siteId);

    @Override
    Optional<Lemmas> findById(Integer integer);

    @Override
    void deleteById(Integer integer);
}
