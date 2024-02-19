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

/** Transactional
 *
 *  */

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public CreatePostResponse createPost(CreatePostRequest request) {

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        Post savedPost = postRepository.save(post);

        return new CreatePostResponse(savedPost.getPostId(), savedPost.getTitle(), savedPost.getContent());
    }

    public ReadPostResponse readPostById(Long postId) {

        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 postId로 조회된 게시글이 없습니다."));

        return new ReadPostResponse(foundPost.getPostId(), foundPost.getTitle(), foundPost.getContent());
    }

    @Transactional
    public UpdatePostResponse updatePost(Long postId, UpdatePostRequest request) {

        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 postId로 조회된 게시글이 없습니다."));

        // Dirty Checking
        foundPost.update(request.getTitle(), request.getContent());

        return new UpdatePostResponse(foundPost.getPostId(), foundPost.getTitle(), foundPost.getContent());
    }


    @Transactional
    public DeletePostResponse deletePost(Long postId) {

        Post foundPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 postId로 조회된 게시글이 없습니다."));

        postRepository.delete(foundPost);

        return new DeletePostResponse(foundPost.getPostId());
    }


    public Page<ReadPostResponse> readAllPost(Pageable pageable) {

        // page로 wrapping된 postPage 가져오기
        Page<Post> postsPage = postRepository.findAll(pageable);

        return  postsPage.map(post -> new ReadPostResponse(
                post.getPostId(),
                post.getTitle(),
                post.getContent()
        ));
    }

}
