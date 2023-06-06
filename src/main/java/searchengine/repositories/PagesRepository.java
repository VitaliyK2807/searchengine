package searchengine.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Pages;
import searchengine.model.Sites;

@Service
@Transactional
public interface PagesRepository extends JpaRepository<Pages, Integer> {

}
