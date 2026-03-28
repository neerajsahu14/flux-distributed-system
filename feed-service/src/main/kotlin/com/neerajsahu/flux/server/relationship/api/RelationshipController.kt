package com.neerajsahu.flux.server.relationship.api

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.relationship.api.dto.FollowActionResponse
import com.neerajsahu.flux.server.relationship.api.dto.FollowRequest
import com.neerajsahu.flux.server.relationship.api.dto.ProfileResponse
import com.neerajsahu.flux.server.relationship.api.dto.ProfileStatsResponse
import com.neerajsahu.flux.server.relationship.api.dto.RelationshipInfoResponse
import com.neerajsahu.flux.server.relationship.service.FollowService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/relationship")
class RelationshipController(
    private val followService: FollowService
) {

    @PostMapping("/follow/{targetUserId}")
    fun toggleFollow(
        @PathVariable targetUserId: Long,
        @RequestBody request: FollowRequest,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<FollowActionResponse> {
        val status = followService.toggleFollow(currentUser, targetUserId,request.requestId)
        return ResponseEntity.ok(FollowActionResponse(status, targetUserId))
    }

    @GetMapping("/info/{targetUserId}")
    fun getRelationshipInfo(
        @PathVariable targetUserId: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<RelationshipInfoResponse> {
        return ResponseEntity.ok(followService.getRelationshipInfo(targetUserId, currentUser.id!!))
    }

    @GetMapping("/stats/{targetUserId}")
    fun getTargetProfileStats(
        @PathVariable targetUserId: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<ProfileStatsResponse> {
        val stats = followService.getProfileStats(targetUserId, currentUser.id!!)
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/stats/me")
    fun getCurrentUserProfileStats(
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<ProfileStatsResponse> {
        val stats = followService.getCurrentUserProfileStats(currentUser)
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/{targetUserId}/followers")
    fun getFollowersWithStatus(
        @PathVariable targetUserId: Long,
        @AuthenticationPrincipal currentUser: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<ProfileResponse>> {
        val followers = followService.getFollowersWithStatus(targetUserId, currentUser.id!!, page, size)
        return ResponseEntity.ok(followers)
    }

    @GetMapping("/{targetUserId}/following")
    fun getFollowingWithStatus(
        @PathVariable targetUserId: Long,
        @AuthenticationPrincipal currentUser: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<ProfileResponse>> {
        val following = followService.getFollowingWithStatus(targetUserId, currentUser.id!!, page, size)
        return ResponseEntity.ok(following)
    }

    @GetMapping("/search")
    fun searchUsers(
        @RequestParam query: String,
        @AuthenticationPrincipal currentUser: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<ProfileResponse>> {
        val searchResults = followService.searchGlobalUsers(query, currentUser.id!!, page, size)
        return ResponseEntity.ok(searchResults)
    }
}
