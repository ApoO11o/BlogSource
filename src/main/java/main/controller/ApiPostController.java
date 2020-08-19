package main.controller;

import main.api.request.RequestComment;
import main.api.request.RequestModeration;
import main.api.request.RequestPost;
import main.api.response.AbstractResponse;
import main.api.response.ResponseCalendar;
import main.api.response.ResponsePost;
import main.api.response.ResponsePostById;
import main.api.response.ResponseResultFalse;
import main.api.response.ResponseTag;
import main.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ApiPostController {

    @Autowired
    private PostService postService;

    @GetMapping(value = "/api/post")
    ResponseEntity<ResponsePost> post(@RequestParam int offset, int limit, String mode) {
        return new ResponseEntity<>(postService.getPostsForMainPage(offset, limit, mode),
            HttpStatus.OK);
    }

    @GetMapping(value = "/api/post/search")
    ResponseEntity<ResponsePost> search(@RequestParam int offset, int limit, String query) {
        return new ResponseEntity<>(postService.getPostsBySearchQuery(offset, limit, query),
            HttpStatus.OK);
    }

    @GetMapping(value = "/api/post/byDate")
    ResponseEntity<ResponsePost> byDate(@RequestParam int offset, int limit, String date) {
        return new ResponseEntity<>(postService.getPostsByDate(offset, limit, date), HttpStatus.OK);
    }

    @GetMapping(value = "/api/post/byTag")
    ResponseEntity<ResponsePost> byTag(@RequestParam int offset, int limit, String tag) {
        return new ResponseEntity<>(postService.getPostsByTag(offset, limit, tag), HttpStatus.OK);
    }

    @GetMapping(value = "/api/post/my")
    ResponseEntity<ResponsePost> my(@RequestParam int offset, int limit, String status) {

        //TODO authorization

        return new ResponseEntity<>(postService.getMyPosts(offset, limit, status), HttpStatus.OK);
    }

    @GetMapping(value = "/api/post/{ID}")
    ResponseEntity<ResponsePostById> postById(@PathVariable int ID) {
        ResponsePostById responsePostById = postService.getPostDataById(ID);
        if (responsePostById == null) {
            return new ResponseEntity<ResponsePostById>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<ResponsePostById>(responsePostById, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/api/post")
    ResponseEntity<AbstractResponse> postPost(RequestPost requestPost) {
        return new ResponseEntity<>(postService.addPost(requestPost), HttpStatus.OK);
    }

    @PostMapping(value = "/api/image", consumes = "multipart/form-data")
    ResponseEntity<Object> image(@RequestParam MultipartFile image) {
        Object response = postService.uploadImage(image);
        if (response.getClass() == ResponseResultFalse.class) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @PutMapping(value = "/api/post/{ID}")
    ResponseEntity<AbstractResponse> changePost(@PathVariable int ID, RequestPost requestPost) {
        return new ResponseEntity<>(postService.changePost(ID, requestPost), HttpStatus.OK);
    }

    @PostMapping(value = "/api/comment")
    ResponseEntity<AbstractResponse> postComment(RequestComment requestComment){
        AbstractResponse response = postService.postComment(requestComment);
        if (response == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @GetMapping(value = "/api/tag")
    ResponseEntity<ResponseTag> getTagList(@RequestParam(required = false) String query){
        return new ResponseEntity<ResponseTag>(postService.getTagList(query), HttpStatus.OK);
    }

    @PostMapping(value = "/api/moderation")
    ResponseEntity<AbstractResponse> moderation(RequestModeration requestModeration){
        return new ResponseEntity<>(postService.changeModerationStatus(requestModeration), HttpStatus.OK);
    }

    @GetMapping(value = "/api/calendar")
    ResponseEntity<ResponseCalendar> calendar(@RequestParam(required = false) Integer year){
        return new ResponseEntity<ResponseCalendar>(postService.getCalendarResponse(year), HttpStatus.OK);
    }
}
