package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Indexes;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexesRepository extends JpaRepository<Indexes, Integer> {
    @Query("select i from Indexes i where i.page.id = ?1")
    Optional<List<Indexes>> findByPageId(int id);
    @Query("select i from Indexes i where i.page.id = ?1 and i.lemma.id = ?2")
    Optional<Indexes> findByPage_IdAndLemma_Id(int pageId, int lemmaId);
    
    @Query("select i from Indexes i where i.lemma.id = ?1")
    List<Indexes> findByLemma_Id(int id);



}
