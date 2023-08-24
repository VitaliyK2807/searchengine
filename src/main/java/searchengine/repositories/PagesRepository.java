package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Pages;
import searchengine.model.Sites;

import java.util.Optional;

@Repository
public interface PagesRepository extends JpaRepository<Pages, Integer> {
    @Query("select p from Pages p where p.path = ?1")
    Optional<Pages> getPageByPath(String path);


}
