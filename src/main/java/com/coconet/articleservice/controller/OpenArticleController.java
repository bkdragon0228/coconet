package com.coconet.articleservice.controller;

import com.coconet.articleservice.common.response.Response;
import com.coconet.articleservice.dto.ArticleFilterDto;
import com.coconet.articleservice.dto.ArticleResponseDto;
import com.coconet.articleservice.dto.client.ChatClientResponseDto;
import com.coconet.articleservice.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/article-service/open-api")
@RequiredArgsConstructor
public class OpenArticleController {

    private final ArticleService articleService;

    @GetMapping("/article/{articleUUID}")
    public Response<ArticleResponseDto> getArticle(@PathVariable String articleUUID){
        ArticleResponseDto article = articleService.getArticle(articleUUID, null);
        return Response.OK(article);
    }

    @PostMapping("/articles")
    public Response<Page<ArticleResponseDto>> getArticles(@RequestBody ArticleFilterDto condition,
                                                Pageable pageable){
        return Response.OK(articleService.getArticles(condition, null, pageable));
    }

    @GetMapping("/popular")
    public Response<List<ArticleResponseDto>> getPopularPosts(){ return Response.OK(articleService.getPopularPosts()); }

    @GetMapping("/chatClient")
    public Response<ChatClientResponseDto> sendChatClient(UUID articleUUID) {
        return Response.OK(articleService.sendChatClient(articleUUID));
    }
}
