package searchengine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Application{
    //private static final Logger log = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        log.info("Start");
        SpringApplication.run(Application.class, args);

    }

}
