package searchengine.dto.siteParsing;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import searchengine.model.Sites;

import java.util.Objects;

@Getter
@Setter

public class IndexedPage {

    private String path;
    private int code;
    private String content;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexedPage that = (IndexedPage) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return "IndexedPage{" +
                "path='" + path + '\'' +
                ", code=" + code +
                '}';
    }
}
