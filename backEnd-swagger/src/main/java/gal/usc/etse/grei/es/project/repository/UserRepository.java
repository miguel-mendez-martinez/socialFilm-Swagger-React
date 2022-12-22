package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.User;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

@Document(collection = "users")
public interface UserRepository extends MongoRepository<User, String> {
}
