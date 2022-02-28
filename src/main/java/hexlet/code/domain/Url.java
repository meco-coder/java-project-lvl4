package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Clock;
import java.time.Instant;

@Entity
public class Url extends Model {
    @Id
    private Long id;

    private final String name;

    @WhenCreated
    private final Instant createdAt;

    public Url(String name) {
        this.name = name;
        this.createdAt = Clock.systemUTC().instant();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatedAt() {
        return createdAt.toString();
    }
}