package org.ohgiraffers.board.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ohgiraffers.board.domain.dto.*;
import org.ohgiraffers.board.domain.entity.Post;
import org.ohgiraffers.board.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTests {

    /** @Mock
     *  가짜 객체, 테스트 실행 시 실제가 아닌 Mock 객체 반환
     *  */
    @Mock
    private PostRepository postRepository;

    /** @InjectMocks
     *  Mock 객체가 주입될 클래스 지정
     *  */
    @InjectMocks
    private PostService postService;

    private Post savedPost;
    private Post post;
    private CreatePostRequest createPostRequest;
    private UpdatePostRequest updatePostRequest;


    @BeforeEach
    void setup() {
        // 초기화
        post = new Post(1L, "테스트 제목", "테스트 내용");
        savedPost = new Post(2L, "저장된 테스트 제목", "저장된 테스트 내용");
        createPostRequest = new CreatePostRequest("테스트 제목", "테스트 내용");
        updatePostRequest = new UpdatePostRequest("변경된 제목", "변경된 내용");
    }


    @Test
    @DisplayName("게시글 작성 기능 테스트")
    void create_post_test() {
        // given
//        when(postRepository.save(any())).thenReturn(post);
        // BDDMockito 형태
        given(postRepository.save(any())).willReturn(post);

        // when
        CreatePostResponse createPostResponse = postService.createPost(createPostRequest);

        // then
        assertThat(createPostResponse.getPostId()).isEqualTo(1L);
        assertThat(createPostResponse.getTitle()).isEqualTo("테스트 제목");
        assertThat(createPostResponse.getContent()).isEqualTo("테스트 내용");
    }


    @Test
    @DisplayName("게시글 단건조회 기능 테스트")
    void read_post_by_id_1() {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.of(savedPost));

        // when
        ReadPostResponse readPostResponse = postService.readPostById(savedPost.getPostId());

        // then
        assertThat(readPostResponse.getPostId()).isEqualTo(savedPost.getPostId());
        assertThat(readPostResponse.getTitle()).isEqualTo(savedPost.getTitle());
        assertThat(readPostResponse.getPostId()).isEqualTo(savedPost.getPostId());
    }


    @Test
    @DisplayName("게시글 단건조회 기능 테스트: postId 게시물 없을 때 Exception 발생 테스트")
    void read_post_by_id_2() {
        // given
        given(postRepository.findById(any())).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> postService.readPostById(1L));
    }


    @Test
    @DisplayName("게시글 전체 조회 기능 테스트")
    void read_all_post_test() {
        // given
        Pageable pageable = PageRequest.of(0, 5);
        List<Post> postList = Arrays.asList(post, savedPost);
        Page<Post> postPage = new PageImpl<>(postList, pageable, postList.size());
        
        given(postRepository.findAll(pageable)).willReturn(postPage);
        
        // when
        Page<ReadPostResponse> responses = postService.readAllPost(pageable);
        
        // then
        assertThat(responses.getContent()).hasSize(2);
        assertThat(responses.getContent().get(0).getTitle()).isEqualTo("테스트 제목");
        assertThat(responses.getContent().get(0).getContent()).isEqualTo("테스트 내용");
        assertThat(responses.getContent().get(1).getTitle()).isEqualTo("저장된 테스트 제목");
        assertThat(responses.getContent().get(1).getContent()).isEqualTo("저장된 테스트 내용");
    }


    @Test
    @DisplayName("게시물 수정 기능 테스트")
    void update_post_test() {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.of(post));

        // when
        UpdatePostResponse updatePostResponse = postService.updatePost(post.getPostId(), updatePostRequest);

        // then
        assertThat(updatePostResponse.getPostId()).isEqualTo(1L);
        assertThat(updatePostResponse.getTitle()).isEqualTo("변경된 제목");
        assertThat(updatePostResponse.getContent()).isEqualTo("변경된 내용");
    }


    @Test
    @DisplayName("게시물 수정 기능 테스트: postId 게시물 없을 때 Exception 발생 테스트")
    void update_post_test2() {
        // given
        given(postRepository.findById(any())).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> postService.updatePost(post.getPostId(), updatePostRequest));
    }


    @Test
    @DisplayName("게시물 삭제 기능 테스트")
    void delete_post_test() {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.of(savedPost));

        // when
        DeletePostResponse response = postService.deletePost(savedPost.getPostId());

        // then
        assertThat(response.getPostId()).isEqualTo(2L);
    }


    @Test
    @DisplayName("게시물 삭제 기능 테스트: postId 게시물 없을 때 Exception 발생 테스트")
    void delete_post_test2() {
        // given
        given(postRepository.findById(any())).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> postService.deletePost(post.getPostId()));
    }


}
