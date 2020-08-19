package main.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import main.api.request.RequestComment;
import main.api.request.RequestModeration;
import main.api.request.RequestPost;
import main.api.response.AbstractResponse;
import main.api.response.ResponseCalendar;
import main.api.response.ResponseComment;
import main.api.response.ResponsePost;
import main.api.response.ResponsePostById;
import main.api.response.ResponseResultFalse;
import main.api.response.ResponseResultTrue;
import main.api.response.ResponseTag;
import main.model.api.CommentResponse;
import main.model.api.PostResponse;
import main.model.api.TagResponse;
import main.model.api.UserResponse;
import main.model.api.UserResponseForComments;
import main.model.entities.ModerationStatus;
import main.model.entities.Post;
import main.model.entities.PostComment;
import main.model.entities.Tag;
import main.model.entities.User;
import main.model.repositories.PostCommentRepository;
import main.model.repositories.PostRepository;
import main.model.repositories.PostVoteRepository;
import main.model.repositories.Tag2PostRepository;
import main.model.repositories.TagRepository;
import main.model.repositories.UserRepository;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostVoteRepository postVoteRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private Tag2PostRepository tag2PostRepository;

    public ResponsePost getPostsForMainPage(int offset, int limit, String mode) {
        ResponsePost responsePost = new ResponsePost();
        responsePost.setCount(getActiveAcceptedPostsCount());
        responsePost.setPosts(getActiveAcceptedPostsList(offset, limit, mode));
        return responsePost;
    }

    private List<PostResponse> getActiveAcceptedPostsList(int offset, int limit, String mode) {
        Iterable<Post> postIterable = null;
        switch (mode) {
            case "recent":
                postIterable = postRepository.findActiveAcceptedRecentPosts(offset, limit);
                break;
            case "popular":
                postIterable = postRepository.findActiveAcceptedPopularPosts(offset, limit);
                break;
            case "best":
                postIterable = postRepository.findActiveAcceptedPosts();
                ((List<Post>) postIterable).sort(((Comparator<Post>) (o1, o2) -> Integer
                    .compare(getPostLikeCount(o1), getPostLikeCount(o2))).reversed());
                break;
            case "early":
                postIterable = postRepository.findActiveAcceptedEarlyPosts(offset, limit);
                break;
        }
        return getListFromPostIterable(postIterable);
    }

    private PostResponse getPostResponse(Post post) {
        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setTimestamp(post.getTime().getTime() / 1000L);
        postResponse.setUser(getUserResponse(post.getUser()));
        postResponse.setTitle(post.getTitle());
        postResponse.setAnnounce(getPostAnnounce(post));
        postResponse.setLikeCount(getPostLikeCount(post));
        postResponse.setDislikeCount(getPostDislikeCount(post));
        postResponse.setCommentCount(getPostCommentCount(post));
        postResponse.setViewCount(post.getViewCount());
        return postResponse;
    }

    private int getPostCommentCount(Post post) {
        return postCommentRepository.findPostCommentCount(post.getId());
    }

    private int getPostDislikeCount(Post post) {
        return postVoteRepository.findDislikeCount(post.getId());
    }

    private int getPostLikeCount(Post post) {
        return postVoteRepository.findLikeCount(post.getId());
    }

    private String getPostAnnounce(Post post) {
        String text = post.getText();
        return Jsoup.parse(text).text();
    }

    private UserResponse getUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        return userResponse;
    }

    private int getActiveAcceptedPostsCount() {
        return postRepository.findActiveAcceptedPostsCount();
    }

    public ResponsePost getPostsBySearchQuery(int offset, int limit, String query) {
        ResponsePost responsePost = new ResponsePost();
        responsePost.setCount(getActiveAcceptedPostsBySearchCount(query));
        responsePost.setPosts(getActiveAcceptedPostsBySearchList(offset, limit, query));
        return responsePost;
    }

    private List getActiveAcceptedPostsBySearchList(int offset, int limit, String query) {
        Iterable<Post> postIterable = null;
        if (!query.isEmpty()) {
            query = "%" + query.toLowerCase() + "%";
            postIterable = postRepository.findActiveAcceptedPostsBySearch(offset, limit, query);
        }
        return getListFromPostIterable(postIterable);
    }

    private int getActiveAcceptedPostsBySearchCount(String query) {
        int count = 0;
        if (!query.isEmpty()) {
            query = "%" + query.toLowerCase() + "%";
            count = postRepository.findActiveAcceptedPostsBySearchCount(query);
        }
        return count;
    }

    public ResponsePost getPostsByDate(int offset, int limit, String date) {
        String from = date + " 00:00:00";
        String to = date + " 23:59:59";
        ResponsePost responsePost = new ResponsePost();
        responsePost.setCount(getPostsByDateCount(date));
        responsePost.setPosts(getPostsByDateList(offset, limit, date));
        return responsePost;
    }

    private List getPostsByDateList(int offset, int limit, String date) {
        String from = date + " 00:00:00";
        String to = date + " 23:59:59";
        Iterable<Post> postIterable = postRepository
            .findActiveAcceptedPostsByDate(from, to, offset, limit);
        return getListFromPostIterable(postIterable);
    }

    private int getPostsByDateCount(String date) {
        String from = date + " 00:00:00";
        String to = date + " 23:59:59";
        return postRepository.findActiveAcceptedPostsByDateCount(from, to);
    }

    private List getListFromPostIterable(Iterable<Post> postIterable) {
        List<PostResponse> postList = new ArrayList<>();
        if (postIterable != null) {
            for (Post post : postIterable) {
                postList.add(getPostResponse(post));
            }
        }
        return postList;
    }

    public ResponsePost getPostsByTag(int offset, int limit, String tag) {
        ResponsePost responsePost = new ResponsePost();
        responsePost.setCount(getPostsByTagCount(tag.toLowerCase()));
        responsePost.setPosts(getPostsByTagList(tag.toLowerCase(), offset, limit));
        return responsePost;
    }

    private List getPostsByTagList(String tag, int offset, int limit) {
        Iterable<Post> postIterable = postRepository
            .findActiveAcceptedPostsByTag(tag, offset, limit);
        return getListFromPostIterable(postIterable);
    }

    private int getPostsByTagCount(String tag) {
        return postRepository.findActiveAcceptedPostsByTagCount(tag);
    }

    public ResponsePost getMyPosts(int offset, int limit, String status) {
        if (status.equals("pending")) {
            status = "NEW";
        } else if (status.equals("published")) {
            status = "ACCEPTED";
        }

        //TODO authorization
        int userId = 88;

        ResponsePost responsePost = new ResponsePost();
        responsePost.setCount(getMyPostsCount(status, userId));
        responsePost.setPosts(getMyPostsList(offset, limit, status, userId));
        return responsePost;
    }

    private List getMyPostsList(int offset, int limit, String status, int userId) {
        Iterable<Post> postIterable = null;
        if (status.equals("inactive")) {
            postIterable = postRepository.findMyInactivePosts(offset, limit, userId);
        } else {
            postIterable = postRepository.findMyPostsByStatus(offset, limit, status, userId);
        }
        return getListFromPostIterable(postIterable);
    }

    private int getMyPostsCount(String status, int userId) {
        int count = 0;
        if (status.equals("inactive")) {
            count = postRepository.findMyInactivePostsCount(userId);
        } else {
            count = postRepository.findMyPostsByStatusCount(status, userId);
        }
        return count;
    }

    public ResponsePostById getPostDataById(int id) {
        Optional<Post> postIterable = postRepository.findAcceptedPostsById(id);
        ResponsePostById responsePostById;
        if (postIterable.isEmpty()) {
            responsePostById = null;
        } else {
            Post post = postIterable.get();

            //TODO authorization
            boolean isModerator = false;
            int userId = 1;

            if (!isModerator && userId != post.getUser().getId()) {
                post.setViewCount(post.getViewCount() + 1);
                postRepository.save(post);
            }

            responsePostById = getPostResponseById(post);
        }
        return responsePostById;
    }

    private ResponsePostById getPostResponseById(Post post) {
        ResponsePostById responsePostById = new ResponsePostById();
        responsePostById.setId(post.getId());
        responsePostById.setTimestamp(post.getTime().getTime() / 1000L);
        responsePostById.setActive(isActive(post.getId()));
        responsePostById.setUser(getUserResponse(post.getUser()));
        responsePostById.setTitle(post.getTitle());
        responsePostById.setText(post.getText());
        responsePostById.setLikeCount(getPostLikeCount(post));
        responsePostById.setDislikeCount(getPostDislikeCount(post));
        responsePostById.setViewCount(post.getViewCount());
        responsePostById.setComments(getPostCommentList(post));
        responsePostById.setTags(getPostTagList(post));
        return responsePostById;
    }

    private List<String> getPostTagList(Post post) {
        Iterable<String> tagIterable = postRepository.findTagNamesByPostId(post.getId());
        List<String> tagList = new ArrayList<>();
        for (String tag : tagIterable) {
            tagList.add(tag);
        }
        return tagList;
    }

    private List<CommentResponse> getPostCommentList(Post post) {
        Iterable<PostComment> postCommentIterable = postCommentRepository
            .findByPostId(post.getId());
        List<CommentResponse> commentResponseList = getListFromPostCommentIterable(
            postCommentIterable);
        return commentResponseList;
    }

    private List<CommentResponse> getListFromPostCommentIterable(
        Iterable<PostComment> postCommentIterable) {
        List<CommentResponse> commentResponses = new ArrayList<>();
        for (PostComment postComment : postCommentIterable) {
            commentResponses.add(getPostCommentResponse(postComment));
        }
        return commentResponses;
    }

    private CommentResponse getPostCommentResponse(PostComment postComment) {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(postComment.getId());
        commentResponse.setTimestamp(postComment.getTime().getTime() / 1000L);
        commentResponse.setText(postComment.getText());
        commentResponse.setUser(getUserResponseForComments(postComment.getUser()));
        return commentResponse;
    }

    private UserResponseForComments getUserResponseForComments(User user) {
        UserResponseForComments userResponseForComments = new UserResponseForComments();
        userResponseForComments.setId(user.getId());
        userResponseForComments.setName(user.getName());
        userResponseForComments.setPhoto(user.getPhoto());
        return userResponseForComments;
    }

    private boolean isActive(int id) {
        boolean isActive = true;
        if (postRepository.findActivityStatusById(id) == 0) {
            isActive = false;
        }
        return isActive;
    }

    public AbstractResponse addPost(RequestPost requestPost) {
        String title = requestPost.getTitle();
        String text = requestPost.getText();
        if (title.length() < 3 || text.length() < 50) {
            ResponseResultFalse responseResultFalse = new ResponseResultFalse();
            HashMap errors = new HashMap();
            if (title.length() < 3) {
                errors.put("title", "Заголовок не установлен или слишком короткий");
            }
            if (text.length() < 50) {
                errors.put("text", "Текст публикации слишком короткий");
            }
            responseResultFalse.setErrors(errors);
            return responseResultFalse;

        } else {
            Timestamp timestamp = new Timestamp(requestPost.getTimestamp());
            Date publishTime = new Date(timestamp.getTime());
            if (publishTime.before(new Date())) {
                publishTime = new Date();
            }

            Post post = new Post();
            post.setModerationStatus(ModerationStatus.NEW);
            post.setTime(publishTime);
            post.setIsActive(requestPost.getActive());
            post.setTitle(requestPost.getTitle());
            post.setText(requestPost.getText());
            post.setViewCount(0);
            post.setTags(addTags(requestPost.getTags(), post.getId()));

            //TODO authorization
            User user = userRepository.findById(88).get();
            post.setUser(user);

            postRepository.save(post);

            return new ResponseResultTrue();
        }
    }

    private List<Tag> addTags(String tags, int postId) {
        String[] tagsArray = tags.split(",");
        List<Tag> tagList = new ArrayList<>();
        for (int i = 0; i < tagsArray.length; i++) {
            String tagName = tagsArray[i].toLowerCase();
            Optional<Tag> tagOptional = tagRepository.findByName(tagName);
            Tag tag = null;
            if (tagOptional.isEmpty()) {
                tag = new Tag();
                tag.setName(tagName);
                tagRepository.save(tag);
            } else {
                tag = tagOptional.get();
            }

            tagList.add(tag);
        }
        return tagList;
    }

    public Object uploadImage(MultipartFile image) {

        //TODO authorization

        if (!image.getContentType().contains("jpeg") && !image.getContentType()
            .contains("png")) {
            ResponseResultFalse responseResultFalse = new ResponseResultFalse();
            HashMap errors = new HashMap();
            errors.put("image", "Отправлен файл не формата изображение jpg, png");
            responseResultFalse.setErrors(errors);
            return responseResultFalse;

        } else {
            String hashCode = String.valueOf(image.hashCode());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("upload/");

            int begin = 0;
            for (int i = 0; i < 4; i++) {
                if (i != 3) {
                    int end = begin + 2;
                    stringBuilder.append(hashCode.substring(begin, end));
                    stringBuilder.append("/");
                    begin = end;
                } else {
                    new File(stringBuilder.toString()).mkdirs();
                    stringBuilder.append(hashCode.substring(begin));
                    stringBuilder.append(".jpg");
                }
            }
            String path = stringBuilder.toString();
            try {
                byte[] bytes = image.getBytes();
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    new FileOutputStream(new File(path)));
                bufferedOutputStream.write(bytes);

                bufferedOutputStream.flush();
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return path;

        }
    }

    public AbstractResponse changePost(int id, RequestPost requestPost) {
        String title = requestPost.getTitle();
        String text = requestPost.getText();
        if (title.length() < 3 || text.length() < 50) {
            ResponseResultFalse responseResultFalse = new ResponseResultFalse();
            HashMap errors = new HashMap();
            if (title.length() < 3) {
                errors.put("title", "Заголовок не установлен или слишком короткий");
            }
            if (text.length() < 50) {
                errors.put("text", "Текст публикации слишком короткий");
            }
            responseResultFalse.setErrors(errors);
            return responseResultFalse;

        } else {
            Optional<Post> postOptional = postRepository.findById(id);
            if (postOptional.isPresent()) {
                Post post = postOptional.get();

                Timestamp timestamp = new Timestamp(requestPost.getTimestamp());
                Date publishTime = new Date(timestamp.getTime());
                if (publishTime.before(new Date())) {
                    publishTime = new Date();
                }

                post.setTime(publishTime);
                post.setIsActive(requestPost.getActive());
                post.setTitle(requestPost.getTitle());
                post.setText(requestPost.getText());
                post.setTags(addTags(requestPost.getTags(), id));

                //TODO authorization, check moderator
                post.setModerationStatus(ModerationStatus.NEW);

                postRepository.save(post);
            }
            return new ResponseResultTrue();
        }
    }

    public AbstractResponse postComment(RequestComment requestComment) {
        String text = requestComment.getText();
        ResponseResultFalse responseResultFalse = new ResponseResultFalse();
        HashMap errors = new HashMap();
        if (text.length() < 3) {
            errors.put("text", "Текст комментария отсутствует (пустой) или слишком короткий");
            responseResultFalse.setErrors(errors);
            return responseResultFalse;

        } else {
            PostComment postComment = new PostComment();
            Post post = getPostById(requestComment.getPost_id());
            if (post == null) {
                return null;
            } else {
                postComment.setPost(post);
                postComment.setText(text);
                postComment.setTime(new Date());

                //TODO authorization
                postComment.setUser(userRepository.findById(88).get());

                if (!requestComment.getParent_id().isEmpty()) {
                    Optional<PostComment> postCommentOptional = postCommentRepository
                        .findById(Integer.parseInt(requestComment.getParent_id()));
                    if (postCommentOptional.isPresent()) {
                        postComment.setParent(postCommentOptional.get());
                    }
                }
                postCommentRepository.save(postComment);
                ResponseComment responseComment = new ResponseComment();
                responseComment.setId(postComment.getId());
                return responseComment;
            }
        }
    }

    private Post getPostById(int postId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        Post post = null;
        if (postOptional.isPresent()) {
            post = postOptional.get();
        }
        return post;
    }

    public ResponseTag getTagList(String query) {
        List<TagResponse> tagsWeightList = getTagsWeight();

        Iterable<Tag> tagIterable = null;
        if (query == null || query.isEmpty()) {
            tagIterable = tagRepository.findAll();
        } else {
            tagIterable = tagRepository.findByNameStartingWith(query);
        }

        List tags = new ArrayList();
        for (Tag tag : tagIterable) {
            for (TagResponse tagResponse : tagsWeightList) {
                if (tagResponse.getName().equals(tag.getName())) {
                    tags.add(tagResponse);
                }
            }
        }

        ResponseTag responseTag = new ResponseTag();
        responseTag.setTags(tags);
        return responseTag;
    }

    private List<TagResponse> getTagsWeight() {
        int postCount = getActiveAcceptedPostsCount();

        HashMap<String, Double> tagName2weight = new HashMap();
        Iterable<Tag> tagIterable = tagRepository.findAll();
        for (Tag tag : tagIterable) {
            double postsWithTagCount = getPostsByTagCount(tag.getName());
            double weight = postsWithTagCount / postCount;
            tagName2weight.put(tag.getName(), weight);
        }

        double max = 0;
        for (Map.Entry<String, Double> entry : tagName2weight.entrySet()) {
            if (entry.getValue().compareTo(max) > 0) {
                max = entry.getValue();
            }
        }

        List tagsWeightList = new ArrayList();
        for (Map.Entry<String, Double> entry : tagName2weight.entrySet()) {
            TagResponse tagResponse = new TagResponse();
            tagResponse.setName(entry.getKey());
            double weight = 1 / (max / entry.getValue());
            tagResponse.setWeight(weight);
            tagsWeightList.add(tagResponse);
        }
        return tagsWeightList;
    }

    public AbstractResponse changeModerationStatus(RequestModeration requestModeration) {

        //TODO authorization
        User user = userRepository.findById(1).get();

        ResponseResultTrue responseResultTrue = new ResponseResultTrue();
        if (user.getIsModerator() == 0) {
            responseResultTrue.setResult(false);
        } else {
            Post post = getPostById(requestModeration.getPost_id());
            if (post == null) {
                responseResultTrue.setResult(false);
            } else {
                ModerationStatus moderationStatus = null;
                if (requestModeration.getDecision().equals("accept")) {
                    moderationStatus = ModerationStatus.ACCEPTED;
                    post.setModerationStatus(moderationStatus);
                    post.setModerator(user);
                } else if (requestModeration.getDecision().equals("decline")) {
                    moderationStatus = ModerationStatus.DECLINED;
                    post.setModerationStatus(moderationStatus);
                    post.setModerator(user);
                } else {
                    responseResultTrue.setResult(false);
                }
                postRepository.save(post);
            }
        }
        return responseResultTrue;
    }

    public ResponseCalendar getCalendarResponse(Integer year) {
        if (year == null) {
            year = Calendar.getInstance().get(Calendar.YEAR);
        }

        ResponseCalendar responseCalendar = new ResponseCalendar();
        List years = postRepository.findDistinctPostYears();
        Collections.sort(years);
        responseCalendar.setYears(years);

        HashMap date2count = new HashMap();
        List<String> uniqueDateList = getDistinctPostDatesByYear(year);
        for (String date : uniqueDateList) {
            int postsCount = getPostsByDateCount(date);
            date2count.put(date, postsCount);
        }
        responseCalendar.setPosts(date2count);

        return responseCalendar;
    }

    private List<String> getDistinctPostDatesByYear(Integer year) {
        String from = year + "-01-01 00:00:00";
        String to = year + "-12-31 23:59:59";
        return  postRepository.findDistinctPostDatesByYear(from, to);
    }
}
