package main.model.repositories;

import main.model.entities.PostVote;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVoteRepository extends CrudRepository<PostVote, Integer> {

    @Query(value = "SELECT COUNT(*) "
        + "FROM post_votes "
        + "WHERE post_id = ?1 "
        + "AND value = 1",
        nativeQuery = true)
    int findLikeCount(int id);

    @Query(value = "SELECT COUNT(*) "
        + "FROM post_votes "
        + "WHERE post_id = ?1 "
        + "AND value = -1",
        nativeQuery = true)
    int findDislikeCount(int id);
}
