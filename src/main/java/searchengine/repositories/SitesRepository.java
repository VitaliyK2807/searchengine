package searchengine.repositories;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Sites;

import java.util.List;

@Repository
public interface SitesRepository extends JpaRepository<Sites, Integer> {
    Sites findByUrl(String url);
}
