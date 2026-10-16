package com.example.board;

import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import com.example.board.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:wordtest;DB_CLOSE_DELAY=-1")
class WordReplaceTest {

    @Autowired PostService service;
    @Autowired PostRepository repository;

    @BeforeEach
    void clear() {
        repository.deleteAll();
    }

    private Long save(String title, String content) {
        Post p = new Post();
        p.setTitle(title);
        p.setName("kim");
        p.setContent(content);
        return repository.save(p).getId();
    }

    @Test
    void 대소문자만_다른_글은_변경_건수에_포함되지_않는다() {
        Long lower = save("kfc 후기", "kfc 최고");
        Long upper = save("KFC 후기", "KFC 최고");

        // 검색은 대소문자를 무시하므로 둘 다 검색된다
        assertThat(service.searchIdsByWord("kfc")).containsExactly(lower, upper);

        int changed = service.replaceWord(List.of(lower, upper), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
        assertThat(repository.findById(lower).get().getTitle()).isEqualTo("bhc 후기");
        assertThat(repository.findById(upper).get().getTitle()).isEqualTo("KFC 후기");
    }

    @Test
    void 존재하지_않는_id는_건너뛴다() {
        Long id = save("kfc", "kfc");
        assertThat(service.replaceWord(List.of(id, 9999L), "kfc", "bhc")).isEqualTo(1);
    }
}
