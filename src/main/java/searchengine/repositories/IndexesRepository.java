package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Indexes;
import searchengine.model.Pages;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexesRepository extends JpaRepository<Indexes, Integer> {
    @Query("select i from Indexes i where i.page.id = ?1 and i.lemma.id = ?2")
    Optional<Indexes> findByPage_IdAndLemma_Id(int id, int id1);
    
    @Query("select i from Indexes i where i.lemma.id = ?1")
    List<Indexes> findByLemma_Id(int id);
    @Transactional
    @Modifying
    @Query("delete from Indexes i where i.page = ?1")
    void DeleteIndexesByPage(int page_id);
    @Query("select i from Indexes i where i.page = ?1")
    List<Indexes> findIndexesByIdPage(int page_id);

}
