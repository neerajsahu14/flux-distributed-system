package com.neerajsahu.flux.server.feed.api

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.feed.service.FeedService
import com.neerajsahu.flux.server.feed.api.dto.CreatePostRequest
import com.neerajsahu.flux.server.feed.api.dto.PostResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/feed")
class FeedController(
    private val feedService: FeedService
) {

    @PostMapping("/post")
    fun createPost(
        @RequestParam("image") file: MultipartFile,
        @RequestParam("caption") caption: String?,
        @RequestParam("requestId") requestId: String, // Idempotency Key (UUID from Mobile)
        @AuthenticationPrincipal user: User
    ): ResponseEntity<PostResponse> {

        val req = CreatePostRequest(
            caption = caption,
            requestId = requestId
        )

        return ResponseEntity.ok(feedService.createPost(user, file, req))
    }
}