package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "Resource",
        description = "A complete film's resource representation"
)
public class Resource {
    @Schema(example = "https://image.tmdb.org/t/p/original/mAqgFQxaBaLkcQBRQf9YnAz9sNQ.jpg")
    private String url;
    @Schema(example = "POSTER")
    private ResourceType type;

    public Resource(ResourceType type, String url) {
        this.type = type;
        this.url = url;
    }

    public Resource() {
    }

    public ResourceType getType() {
        return type;
    }

    public Resource setType(ResourceType type) {
        this.type = type;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Resource setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(url, resource.url) && type == resource.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, type);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Resource.class.getSimpleName() + "[", "]")
                .add("url='" + url + "'")
                .add("type=" + type)
                .toString();
    }
}
