package main.model.repositories;

import java.util.Optional;
import main.model.entities.Tag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends CrudRepository<Tag, Integer> {

    Optional<Tag> findByName(String tagName);

    Iterable<Tag> findByNameStartingWith(String query);
}
