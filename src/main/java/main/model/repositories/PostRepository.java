package main.model.repositories;

import java.util.List;
import java.util.Optional;
import main.model.entities.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends CrudRepository<Post, Integer> {

    @Query(value =
        "SELECT COUNT(*) FROM posts"
            + " WHERE is_active = 1"
            + " AND moderation_status = 'ACCEPTED'"
            + " AND time <= current_timestamp()",
        nativeQuery = true)
    int findActiveAcceptedPostsCount();

    @Query(value = "SELECT * FROM posts"
        + " WHERE is_active = 1 and moderation_status = 'ACCEPTED'"
        + " AND time <= current_timestamp()"
        + " ORDER BY time"
        + " DESC"
        + " LIMIT ?1, ?2",
        nativeQuery = true)
    List<Post> findActiveAcceptedRecentPosts(int offset, int limit);

    @Query(value = "SELECT posts.*,"
        + " COUNT(post_comments.post_id) AS comment_count FROM posts"
        + " LEFT JOIN post_comments ON posts.id = post_comments.post_id"
        + " WHERE is_active = 1"
        + " AND moderation_status = 'ACCEPTED'"
        + " AND posts.time <= current_timestamp()"
        + " GROUP BY posts.id"
        + " ORDER BY comment_count"
        + " DESC"
        + " LIMIT ?1, ?2",
        nativeQuery = true)
    List<Post> findActiveAcceptedPopularPosts(int offset, int limit);

    @Query(value = "SELECT * FROM posts"
        + " WHERE is_active = 1 and moderation_status = 'ACCEPTED'"
        + " AND time <= current_timestamp()"
        + " ORDER BY time"
        + " LIMIT ?1, ?2",
        nativeQuery = true)
    List<Post> findActiveAcceptedEarlyPosts(int offset, int limit);

    @Query(value = "SELECT * FROM posts"
        + " WHERE is_active = 1"
        + " AND moderation_status = 'ACCEPTED'"
        + " AND time <= current_timestamp()",
        nativeQuery = true)
    Iterable<Post> findActiveAcceptedPosts();

    @Query(value = "SELECT COUNT(*) FROM posts"
        + " WHERE is_active = 1"
        + " AND moderation_status = 'ACCEPTED'"
        + " AND time <= current_timestamp() "
        + "AND (title LIKE ?1 OR text LIKE ?1)",
        nativeQuery = true)
    int findActiveAcceptedPostsBySearchCount(String query);

    @Query(value = "SELECT * FROM posts"
        + " WHERE is_active = 1"
        + " AND moderation_status = 'ACCEPTED'"
        + " AND time <= current_timestamp()"
        + " AND (title LIKE ?3 OR text LIKE ?3)"
        + " ORDER BY time"
        + " DESC"
        + " LIMIT ?1, ?2",
        nativeQuery = true)
    Iterable<Post> findActiveAcceptedPostsBySearch(int offset, int limit, String query);

    @Query(value = "SELECT * FROM posts"
        + " WHERE time between ?1 and ?2"
        + " AND is_active = 1"
        + " AND moderation_status = 'ACCEPTED'"
        + " AND time <= current_timestamp()"
        + " ORDER BY time"
        + " DESC"
        + " LIMIT ?3, ?4",
        nativeQuery = true)
    Iterable<Post> findActiveAcceptedPostsByDate(String from, String to, int offset, int limit);

    @Query(value = "SELECT COUNT(*) FROM posts"
        + " WHERE time between ?1 and ?2"
        + " AND is_active = 1"
        + " AND moderation_status = 'ACCEPTED'"
        + " AND time <= current_timestamp()",
        nativeQuery = true)
    int findActiveAcceptedPostsByDateCount(String from, String to);


    @Query(value = "SELECT posts.* FROM posts"
        + " LEFT JOIN tag2post ON posts.id = tag2post.post_id"
        + " LEFT JOIN tags ON tag2post.tag_id = tags.id"
        + " WHERE tags.name = ?1"
        + " AND is_active = 1"
        + " AND moderation_status = 'ACCEPTED'"
        + " AND posts.time <= current_timestamp()"
        + " ORDER BY time"
        + " DESC"
        + " LIMIT ?2, ?3",
        nativeQuery = true)
    Iterable<Post> findActiveAcceptedPostsByTag(String tag, int offset, int limit);

    @Query(value = "SELECT COUNT(*) FROM posts"
        + " LEFT JOIN tag2post ON posts.id = tag2post.post_id"
        + " LEFT JOIN tags ON tag2post.tag_id = tags.id"
        + " WHERE tags.name = ?1"
        + " AND is_active = 1"
        + " AND moderation_status = 'ACCEPTED'"
        + " AND posts.time <= current_timestamp()",
        nativeQuery = true)
    int findActiveAcceptedPostsByTagCount(String tag);

    @Query(value = "SELECT COUNT(*) "
        + "FROM posts "
        + "WHERE is_active = 0 "
        + "AND user_id = ?1",
        nativeQuery = true)
    int findMyInactivePostsCount(int userId);

    @Query(value = "SELECT COUNT(*)"
        + " FROM posts"
        + " WHERE moderation_status = ?1"
        + " AND is_active = 1"
        + " AND user_id = ?2",
        nativeQuery = true)
    int findMyPostsByStatusCount(String status, int userId);

    @Query(value = "SELECT * FROM posts"
        + " WHERE is_active = 0"
        + " AND user_id = ?3"
        + " ORDER BY time LIMIT ?1, ?2",
        nativeQuery = true)
    Iterable<Post> findMyInactivePosts(int offset, int limit, int userId);

    @Query(value = "SELECT * FROM posts"
        + " WHERE moderation_status = ?3"
        + " AND is_active = 1"
        + " AND user_id = ?4"
        + " ORDER BY time"
        + " DESC"
        + " LIMIT ?1, ?2",
        nativeQuery = true)
    Iterable<Post> findMyPostsByStatus(int offset, int limit, String status, int userId);

    @Query(value = "SELECT tags.name FROM tags"
        + " LEFT JOIN tag2post ON tags.id = tag2post.tag_id"
        + " LEFT JOIN posts ON tag2post.post_id = posts.id"
        + " WHERE posts.id = ?1",
        nativeQuery = true)
    Iterable<String> findTagNamesByPostId(int id);

    @Query(value = "SELECT is_active FROM posts"
        + " WHERE id = ?1",
        nativeQuery = true)
    int findActivityStatusById(int id);

    @Query(value = "SELECT * FROM posts"
        + " WHERE moderation_status = 'ACCEPTED'"
        + " AND id = ?1"
        + " AND time <= current_timestamp()",
        nativeQuery = true)
    Optional<Post> findAcceptedPostsById(int id);

    @Query(value = "SELECT DISTINCT YEAR(time) FROM posts"
        + " WHERE moderation_status = 'ACCEPTED'"
        + " AND is_active = 1"
        + " AND time <= current_timestamp()",
        nativeQuery = true)
    List findDistinctPostYears();

    @Query(value = "SELECT DISTINCT CAST(TIME as DATE) FROM posts"
        + " WHERE moderation_status = 'ACCEPTED'"
        + " AND is_active = 1"
        + " AND time <= current_timestamp()"
        + " AND time BETWEEN ?1 and ?2",
        nativeQuery = true)
    List<String> findDistinctPostDatesByYear(String from, String to);
}
