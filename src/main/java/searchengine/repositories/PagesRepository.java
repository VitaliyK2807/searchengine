package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import searchengine.model.Pages;

@Service
public interface PagesRepository extends JpaRepository<Pages, Integer> {
}
