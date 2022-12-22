package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Friendship;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

@Document(collection = "friends")
public interface FriendShipRepository extends MongoRepository<Friendship, String> {
}
