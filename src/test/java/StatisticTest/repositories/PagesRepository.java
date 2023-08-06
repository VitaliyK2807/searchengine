package StatisticTest.repositories;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import searchengine.model.Pages;

@Service
@Configuration
public interface PagesRepository extends JpaRepository<Pages, Integer> {


}
