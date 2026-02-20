package com.neerajsahu.flux.server.interaction.api

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.feed.api.dto.PostResponse
import com.neerajsahu.flux.server.interaction.api.dto.InteractionRequest
import com.neerajsahu.flux.server.interaction.api.dto.InteractionResponse
import com.neerajsahu.flux.server.interaction.service.InteractionService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/interaction")
class InteractionController(
    private val interactionService: InteractionService
) {

    @PostMapping("/post/{postId}/like")
    fun likePost(
        @PathVariable postId: Long,
        @RequestBody request: InteractionRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<InteractionResponse> {
        return ResponseEntity.ok(interactionService.likePost(user, postId, request.requestId))
    }

    @DeleteMapping("/post/{postId}/like")
    fun unlikePost(
        @PathVariable postId: Long,
        @RequestBody request: InteractionRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<InteractionResponse> {
        return ResponseEntity.ok(interactionService.unlikePost(user, postId, request.requestId))
    }

    @PostMapping("/post/{postId}/bookmark")
    fun bookmarkPost(
        @PathVariable postId: Long,
        @RequestBody request: InteractionRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<InteractionResponse> {
        return ResponseEntity.ok(interactionService.bookmarkPost(user, postId, request.requestId))
    }

    @DeleteMapping("/post/{postId}/bookmark")
    fun unbookmarkPost(
        @PathVariable postId: Long,
        @RequestBody request: InteractionRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<InteractionResponse> {
        return ResponseEntity.ok(interactionService.unbookmarkPost(user, postId, request.requestId))
    }

    @PostMapping("/post/{postId}/share")
    fun sharePost(
        @PathVariable postId: Long,
        @RequestBody request: InteractionRequest,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<InteractionResponse> {
        return ResponseEntity.ok(interactionService.sharePost(user, postId, request.requestId))
    }

    @GetMapping("/bookmarks")
    fun getBookmarkedPosts(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<PostResponse>> {
        return ResponseEntity.ok(interactionService.getBookmarkedPosts(user, page, size))
    }

    @GetMapping("/likes")
    fun getLikedPosts(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<PostResponse>> {
        return ResponseEntity.ok(interactionService.getLikedPosts(user, page, size))
    }

    @GetMapping("/post/{postId}/liked")
    fun isPostLiked(
        @PathVariable postId: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Map<String, Boolean>> {
        return ResponseEntity.ok(mapOf("liked" to interactionService.isPostLikedByUser(user.id!!, postId)))
    }

    @GetMapping("/post/{postId}/bookmarked")
    fun isPostBookmarked(
        @PathVariable postId: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Map<String, Boolean>> {
        return ResponseEntity.ok(mapOf("bookmarked" to interactionService.isPostBookmarkedByUser(user.id!!, postId)))
    }
}