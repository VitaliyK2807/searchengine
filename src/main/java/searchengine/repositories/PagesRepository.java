package searchengine.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Pages;
import searchengine.model.Sites;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagesRepository extends JpaRepository<Pages, Integer> {

    long countBySiteId(Sites siteId);
    @Query("select count(p) from Pages p")
    long countFirstBy();

    @Query("select p from Pages p where p.path = ?1")
    Optional<Pages> getPageByPath(String path);

    @Query("select count(*) from Pages p")
    int getTotalPages ();

    @Query("select count(*) from Pages p where p.siteId = ?1")
    int getTotalPagesSite(Sites siteId);

    @Override
    Optional<Pages> findById(Integer integer);
}
