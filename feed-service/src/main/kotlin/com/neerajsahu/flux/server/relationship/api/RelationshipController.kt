package com.neerajsahu.flux.server.relationship.api

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.auth.api.dto.UserResponse
import com.neerajsahu.flux.server.relationship.api.dto.FollowActionResponse
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

    // 1. Follow / Unfollow Toggle
    @PostMapping("/follow/{targetUserId}")
    fun toggleFollow(
        @PathVariable targetUserId: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<FollowActionResponse> {
        val status = followService.toggleFollow(currentUser, targetUserId)
        return ResponseEntity.ok(FollowActionResponse(status, targetUserId))
    }

    // 2. Get Followers List (Pagination)
    @GetMapping("/followers/{userId}")
    fun getFollowers(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(followService.getFollowers(userId, page, size))
    }

    // 3. Get Following List (Pagination)
    @GetMapping("/following/{userId}")
    fun getFollowing(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(followService.getFollowing(userId, page, size))
    }

    // 4. Get Profile Stats (Counts + IsFollowing status)
    // Jab kisi ki profile khologe, ye API call hogi header setup karne ke liye
    @GetMapping("/info/{targetUserId}")
    fun getRelationshipInfo(
        @PathVariable targetUserId: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<RelationshipInfoResponse> {
        return ResponseEntity.ok(followService.getRelationshipInfo(targetUserId, currentUser.id!!))
    }
    // Get Complete Profile Stats
    @GetMapping("/stats/{targetUserId}")
    fun getProfileStats(
        @PathVariable targetUserId: Long,
        @AuthenticationPrincipal currentUser: User
    ): ResponseEntity<ProfileStatsResponse> {
        val stats = followService.getProfileStats(targetUserId, currentUser.id!!)
        return ResponseEntity.ok(stats)
    }
}