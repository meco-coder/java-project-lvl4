package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

@Entity
public final class Url extends Model {
    @Id
    private long id;

    private String name;

    @WhenCreated
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL)
    private List<UrlCheck> urlCheckList;

    public Url(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<UrlCheck> getUrlCheckList() {
        return urlCheckList;
    }

    public int lastStatusCodeFromUrlCheckList() {
        final UrlCheck urlCheck = urlCheckList.get(urlCheckList.size() - 1);
        return urlCheck.getStatusCode();
    }

    public Instant lastCreatedAtFromUrlCheckList() {

        if (urlCheckList.isEmpty()) {
            return Instant.parse("");
        }

        return urlCheckList.get(urlCheckList.size() - 1).getCreatedAt();
    }

}
