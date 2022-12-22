package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Film;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

@Document(collection = "films")
public interface MovieRepository extends MongoRepository<Film, String> {
}
