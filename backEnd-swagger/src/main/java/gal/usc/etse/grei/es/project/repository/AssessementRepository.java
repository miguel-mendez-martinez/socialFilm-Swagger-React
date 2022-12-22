package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Assessment;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

@Document(collection = "assessment")
public interface AssessementRepository extends MongoRepository<Assessment, String> {
}
