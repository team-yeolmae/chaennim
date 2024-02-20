package org.ohgiraffers.board.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.ohgiraffers.board.domain.dto.*;
import org.ohgiraffers.board.domain.entity.Post;
import org.ohgiraffers.board.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;

/** Service를 인터페이스와 구현체로 나누는 이유
 * 1) 다형성과 OCP 원칙을 지키기 위해 : 구현체는 독립되어 구현체의 수정이나 확장이 자유로워짐
 * 2) 관습적인 추상화 방식 :
 * 과거 spring에서 AOP 구현 시 JDK Dynamic Proxy를 사용했는데, 이때 인터페이스가 필수였기 때문
 * 지금은 CGLB를 기본으로 포함하여 클래스 기반으로 프록시 객체 생성이 가능해짐
 * */

@Service
@Transactional(readOnly = true)   // 해당 메소드에서 데이터를 수정하지 않고 읽기 작업만 수행
@RequiredArgsConstructor   // 필드를 초기화하는 생성자 자동 생성
public class PostService {

    // Post 엔티티에 대한 CRUD 수행을 위해 사용되는 레포지토리
    private final PostRepository postRepository;

    @Transactional
    public CreatePostResponse createPost(CreatePostRequest request) {

        // request의 데이터를 이용하여 신규 게시물 객체 생성(builder 패턴 사용)
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        // 레포지토리를 이용하여 게시물 저장
        Post savedPost = postRepository.save(post);

        // 저장된 결과를 CreatePostResponse 객체로 반환
        return new CreatePostResponse(savedPost.getPostId(), savedPost.getTitle(), savedPost.getContent());
    }

    public ReadPostResponse readPostById(Long postId) {

        // 주어진 postId로 특정 게시물 조회
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 postId로 조회된 게시글이 없습니다."));  // 해당 게시물 없을 경우 예외 처리

        // 조회된 결과 foundPost를 반환
        return new ReadPostResponse(foundPost.getPostId(), foundPost.getTitle(), foundPost.getContent());
    }

    @Transactional
    public UpdatePostResponse updatePost(Long postId, UpdatePostRequest request) {

        // 주어진 postId로 특정 게시물 조회
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 postId로 조회된 게시글이 없습니다."));

        // Dirty Checking : 객체 상태를 모니터링하고 변경 시 자동으로 DB 업데이트. ORM 프레임워크(hibernate)에서 사용.
        foundPost.update(request.getTitle(), request.getContent());

        // update된 새로운 foundPost를 반환
        return new UpdatePostResponse(foundPost.getPostId(), foundPost.getTitle(), foundPost.getContent());
    }

    @Transactional
    public DeletePostResponse deletePost(Long postId) {

        // 주어진 postId로 특정 게시물 조회
        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 postId로 조회된 게시글이 없습니다."));

        postRepository.delete(foundPost);

        // 삭제된 foundPost의 postId 반환
        return new DeletePostResponse(foundPost.getPostId());
    }


    public Page<ReadPostResponse> readAllPost(Pageable pageable) {

        // page로 wrapping된 postPage 가져오기
        Page<Post> postsPage = postRepository.findAll(pageable);

        // 가져온 postPage를 ReadPostResponse 형태로 반환
        return  postsPage.map(post -> new ReadPostResponse(
                post.getPostId(),
                post.getTitle(),
                post.getContent()
        ));
    }

}
