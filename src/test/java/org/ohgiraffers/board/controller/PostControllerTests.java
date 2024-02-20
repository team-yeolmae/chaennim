package org.ohgiraffers.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ohgiraffers.board.domain.dto.*;
import org.ohgiraffers.board.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 통합 테스트 & 단위 테스트
 *  통합 테스트: 모듈 통합 과정에서 모듈간 호환성 확인을 위한 테스트
 *  API의 모든 과정이 올바르게 동작하는지 확인
 *
 *  단위 테스트(Unit Test)
 *  하나의 모듈을 기준으로 독립적으로 진행되는 가장 작은 단위(=메소드 단위)의 테스트
 *  어플리케이션을 구성하는 하나의 기능이 올바르게 동작하는지 독립적으로 테스트
 * */

/** WebMvcTest
 *  MVC를 위한 테스트로, 컨트롤러가 예상대로 동작하는지 테스트
 *  웹 어플리케이션을 어플리케이션 서버에 배포하지 않고 테스트용 MVC 환경을 만들어 요청 및 전송 응답기능 제공
 *
 *  의존성에서 잘라내어 메소드가 제대로 작동하는지 확인하는 과정
 *
 *  MockMvc
 *  스프링 프레임워크에서 사용하는 컨트롤러 테스트용 라이브러리
 *  주로 웹 애플리케이션의 HTTP 요청을 모의(Mock)하고, 이 요청에 대한 응답을 검증
 * */
@WebMvcTest(PostController.class)
public class PostControllerTests {


    @Autowired
    MockMvc mockMvc;

    @MockBean
    PostService postService;

    @Autowired
    ObjectMapper objectMapper;


    @Test
    @DisplayName("게시글 작성 기능 테스트")
    void create_post_test() throws Exception {
        // given
        // dummy request 생성
        CreatePostRequest request = new CreatePostRequest("테스트 제목", "테스트 내용");
        // dummy response 생성 : 게시글이 성공적으로 작성되었을 때 반환되는 응답 모방
        CreatePostResponse response = new CreatePostResponse(1L, "테스트 제목", "테스트 내용");

        // postService.createPost(any()) 메소드는 response 객체를 반환하도록 설정
        given(postService.createPost(any())).willReturn(response);

        // when & then
        // post 요청을 엔드포인트로 전송
        mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andDo(print());
    }


    @Test
    @DisplayName("게시글 단일 조회 기능 테스트")
    void read_post_test() throws Exception {
        // given
        Long postId = 1L;
        ReadPostResponse response = new ReadPostResponse(1L, "테스트 제목", "테스트 내용");

        given(postService.readPostById(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 제목"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andDo(print());
    }


    @Test
    @DisplayName("게시글 수정 기능 테스트")
    void update_post_test() throws Exception {
        // given
        Long postId = 1L;
        UpdatePostRequest request = new UpdatePostRequest("change title", "change content");
        UpdatePostResponse response = new UpdatePostResponse(1L, "change title", "change content");

        given(postService.updatePost(any(), any())).willReturn(response);

        // when & then
        // post 요청을 엔드포인트로 전송
        mockMvc.perform(put("/api/v1/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(1L))
                .andExpect(jsonPath("$.title").value("change title"))
                .andExpect(jsonPath("$.content").value("change content"))
                .andDo(print());
    }


    @Test
    @DisplayName("게시글 삭제 기능 테스트")
    void delete_post_test() throws Exception {
        // given
        Long postId = 1L;
        DeletePostResponse response = new DeletePostResponse(1L);

        given(postService.deletePost(any())).willReturn(response);

        // when & then
        mockMvc.perform(delete("/api/v1/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(1L))
                .andDo(print());
    }


    @Test
    @DisplayName("게시글 전체목록 조회 기능 테스트")
    void read_all_posts_test() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0,5);

        List<ReadPostResponse> responses = new ArrayList<>();
        responses.add(new ReadPostResponse(1L, "테스트 제목1", "테스트 내용1"));
        responses.add(new ReadPostResponse(2L, "테스트 제목2", "테스트 내용2"));
        responses.add(new ReadPostResponse(3L, "테스트 제목3", "테스트 내용3"));

        Page<ReadPostResponse> listResponses = new PageImpl<>(responses, pageable, responses.size());

        given(postService.readAllPost(any())).willReturn(listResponses);

        // when & then
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].postId").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("테스트 제목1"))
                .andExpect(jsonPath("$.content[0].content").value("테스트 내용1"))
                .andExpect(jsonPath("$.content[1].postId").value(2L))
                .andExpect(jsonPath("$.content[1].title").value("테스트 제목2"))
                .andExpect(jsonPath("$.content[1].content").value("테스트 내용2"))
                .andExpect(jsonPath("$.content[2].postId").value(3L))
                .andExpect(jsonPath("$.content[2].title").value("테스트 제목3"))
                .andExpect(jsonPath("$.content[2].content").value("테스트 내용3"))
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 전체목록 조회 기능 테스트2")
    void read_all_posts_test2() throws Exception {
        // given
        int page = 0;
        int size = 5;
        PageRequest pageRequest = PageRequest.of(page, size);
        ReadPostResponse response = new ReadPostResponse(1L, "테스트 제목", "테스트 내용");

        List<ReadPostResponse> responses = new ArrayList<>();
        responses.add(response);

        Page<ReadPostResponse> pageResponse = new PageImpl<>(responses, pageRequest, responses.size());

        given(postService.readAllPost(any())).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].postId").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("테스트 제목1"))
                .andExpect(jsonPath("$.content[0].content").value("테스트 내용1"))
                .andDo(print());
    }

}

