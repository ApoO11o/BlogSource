package main.model.repositories;

import main.model.entities.PostComment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends CrudRepository<PostComment, Integer> {

    @Query(value = "SELECT COUNT(*) "
        + "FROM post_comments "
        + "WHERE post_id = ?1",
        nativeQuery = true)
    int findPostCommentCount(int id);

    Iterable<PostComment> findByPostId(int id);
}
