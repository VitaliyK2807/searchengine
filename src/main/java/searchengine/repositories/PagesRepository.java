package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import searchengine.model.Pages;
import searchengine.model.Sites;

@Service
public interface PagesRepository extends JpaRepository<Pages, Integer> {
    void deleteBySite(Sites site);



}