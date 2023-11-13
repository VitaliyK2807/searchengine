package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Indexes;
import searchengine.model.Pages;

import java.util.List;

@Repository
public interface IndexesRepository extends JpaRepository<Indexes, Integer> {
    @Transactional
    @Modifying
    @Query("delete from Indexes i where i.page = ?1")
    void DeleteIndexesByPage(int page_id);
    @Query("select i from Indexes i where i.page = ?1")
    List<Indexes> findIndexesByIdPage(int page_id);

}
