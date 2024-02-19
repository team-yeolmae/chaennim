package org.ohgiraffers.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ohgiraffers.board.domain.dto.*;
import org.ohgiraffers.board.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 레이어드 아키텍쳐
 * 소프트웨어를 여러 개의 계층으로 분리해서 설계하는 방법
 * 각 계층이 독립적으로 구성되어서 한 계층이 변경되어도 다른 계층에 영향을 주지 않음
 * 결과적으로 코드의 재사용성과 유지보수성 향상
 * */

/** REST란
 * Representational Astate Transfer
 * 자원을 이름으로 구분하여 자원의 상태를 주고받는 것
 * REST는 기본적으로 웹의 기존 기술과 HTTP 프로토콜을 그대로 사용하기 때문에,
 * 웹의 장점을 최대한 활용할 수 있는 아키텍쳐
 * */

@Tag(name = "posts", description = "게시판 API")
@RestController
@ResponseBody
@RequestMapping("/api/v1/posts")
// @RequiredArgsConstructor : final 혹은 @NonNull 어노테이션이 붙은 필드에 대한 생성자 자동 생성
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(
            summary = "게시물을 등록하는 메소드", description = "title, content를 입력하세요.")
    public ResponseEntity<CreatePostResponse> postCreate(@RequestBody CreatePostRequest request) {
        // @RequestBody : HTTP 요청의 본문(body)을 Java 객체로 변환해주는 역할

        CreatePostResponse response = postService.createPost(request);

        // 성공했다는 status를 담아서 response 보내기
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // @PathVariable :
    @GetMapping("/{postId}")
    public ResponseEntity<ReadPostResponse> postRead(@PathVariable Long postId) {

        ReadPostResponse response = postService.readPostById(postId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<UpdatePostResponse> postUpdate(@PathVariable Long postId, @RequestBody UpdatePostRequest request) {

        UpdatePostResponse response = postService.updatePost(postId, request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<DeletePostResponse> postDelete(@PathVariable Long postId) {

        DeletePostResponse response = postService.deletePost(postId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<ReadPostResponse>> postReadAll(
            @PageableDefault(size = 5, sort = "postId", direction = Sort.Direction.DESC) Pageable pageable) {
        // 페이징 처리
        Page<ReadPostResponse> responses = postService.readAllPost(pageable);

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
