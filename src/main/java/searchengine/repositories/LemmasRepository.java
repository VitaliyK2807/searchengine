package searchengine.repositories;

import org.springframework.context.annotation.Configuration;
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

    @Query("select l from Lemmas l where l.lemma = ?1")
    List<Lemmas> findByLemma(String lemma);
    @Transactional
    @Modifying
    @Query("delete from Lemmas l where l.id = ?1")
    void deleteLemmaById(Lemmas lemma);

    @Query("select l from Lemmas l where l.lemma = ?1 and l.site.id = ?2")
    Optional<Lemmas> findByLemmaAndIdSite(String lemma, int id);
    @Transactional
    @Modifying
    @Query("update Lemmas l set l.frequency = ?1 where l.id = ?2")
    void updateLemma(int frequency, Lemmas lemma);

    @Query("select count(*) from Lemmas l")
    int getTotalLemmas ();

    @Query("select count(*) from Lemmas l where l.site.id = ?1")
    int getTotalLemmasSites (int id);
}
