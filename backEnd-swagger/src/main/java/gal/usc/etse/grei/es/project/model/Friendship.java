package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "friends")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "FriendShip",
        description = "A complete friendship between users representation"
)
public class Friendship {
    @Id
    private String id;
    @Schema(example = "test@test.com")
    private String user;
    @Schema(example = "testFriend@test.com")
    private String friend;
    @Schema(example = "false")
    private Boolean confirmed;
    private Date since;

    public Friendship() {
    }

    public Friendship(String id, String user, String friend, Boolean confirmed, Date since) {
        this.id = id;
        this.user = user;
        this.friend = friend;
        this.confirmed = confirmed;
        this.since = since;
    }

    public String getId() {
        return id;
    }
    public String getUser() {
        return user;
    }
    public String getFriend() {
        return friend;
    }
    public Boolean getConfirmed() {
        return confirmed;
    }
    public Date getSince() {
        return since;
    }

    public Friendship setId(String id) {
        this.id = id;
        return this;
    }
    public Friendship setUser(String user) {
        this.user = user;
        return this;
    }
    public Friendship setFriend(String friend) {
        this.friend = friend;
        return this;
    }
    public Friendship setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
        return this;
    }
    public Friendship setSince(Date since) {
        this.since = since;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friendship)) return false;
        Friendship that = (Friendship) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Friendship.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("user='" + user + "'")
                .add("friend='" + friend + "'")
                .add("confirmed=" + confirmed)
                .add("since=" + since)
                .toString();
    }
}
