package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "Crew",
        description = "A complete film's crew representation"
)
public class Crew extends Person {
    @Schema(example = "jobTest")
    private String job;

    public Crew() {
    }

    public String getJob() {
        return job;
    }

    public Crew setJob(String job) {
        this.job = job;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Crew crew = (Crew) o;
        return Objects.equals(job, crew.job);
    }

    @Override
    public int hashCode() {
        return Objects.hash(job);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Crew.class.getSimpleName() + "[", "]")
                .add("job='" + job + "'")
                .toString();
    }
}
