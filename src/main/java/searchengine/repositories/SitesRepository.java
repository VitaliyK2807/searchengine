package searchengine.repositories;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Sites;
import searchengine.model.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SitesRepository extends JpaRepository<Sites, Integer> {
    @Transactional
    @Modifying
    @Query("update Sites s set s.status = ?1 where s.id = ?2")
    int updateStatusById(Status status, int id);
    @Transactional
    @Modifying
    @Query("update Sites s set s.status = ?1, s.lastError = ?2, s.statusTime = ?3 where s.id = ?4")
    int updateFailed(Status status, String lastError, LocalDateTime statusTime, int id);
    @Transactional
    @Modifying
    @Query("update Sites s set s.statusTime = ?1 where s.id = ?2")
    void updateTime(LocalDateTime statusTime, int id);
    Sites findByUrl(String url);

}
