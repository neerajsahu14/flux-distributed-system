package com.neerajsahu.flux.server.relationship.service

import com.neerajsahu.flux.server.auth.api.dto.UserResponse
import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.auth.domain.repository.UserRepository
import com.neerajsahu.flux.server.feed.domain.repository.PostRepository
import com.neerajsahu.flux.server.relationship.api.dto.ProfileStatsResponse
import com.neerajsahu.flux.server.relationship.api.dto.RelationshipInfoResponse
import com.neerajsahu.flux.server.relationship.domain.model.Follow
import com.neerajsahu.flux.server.relationship.domain.model.FollowId
import com.neerajsahu.flux.server.relationship.domain.repository.FollowRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) {

    @Transactional
    fun toggleFollow(follower: User, followeeId: Long, requestId: String): String { // FIX 1: Accept requestId
        if (follower.id == followeeId) {
            throw RuntimeException("You cannot follow yourself")
        }

        if (followRepository.existsByRequestId(requestId)) {
            return "Already processed"
        }

        val followee = userRepository.findById(followeeId)
            .orElseThrow { RuntimeException("User not found") }

        val existingFollow = followRepository.findRelationship(follower.id!!, followeeId)

        return if (existingFollow.isPresent) {
            val follow = existingFollow.get()

            follow.isValid = !follow.isValid

            follow.requestId = requestId

            followRepository.save(follow)

            if (follow.isValid) "Followed" else "Unfollowed"

        } else {
            val newFollow = Follow(
                id = FollowId(follower.id, followeeId),
                follower = follower,
                followee = followee,
                requestId = requestId,
                isValid = true
            )
            followRepository.save(newFollow)
            "Followed"
        }
    }
    fun getFollowers(userId: Long, page: Int, size: Int): List<UserResponse> {
        val pageable = PageRequest.of(page, size)
        val usersPage = followRepository.findFollowersByUserId(userId, pageable)

        // Auth module ka mapper use kar sakte ho ya manual map kar lo
        return usersPage.content.map { user ->
            UserResponse(
                id = user.id!!,
                username = user.username,
                email = user.email,
                bio = user.bio,
                profilePicUrl = user.profilePicUrl
            )
        }
    }

    // 2. Get Following List
    fun getFollowing(userId: Long, page: Int, size: Int): List<UserResponse> {
        val pageable = PageRequest.of(page, size)
        val usersPage = followRepository.findFollowingByUserId(userId, pageable)

        return usersPage.content.map { user ->
            UserResponse(
                id = user.id!!,
                username = user.username,
                email = user.email,
                bio = user.bio,
                profilePicUrl = user.profilePicUrl
            )
        }
    }

    // 3. Get Counts & Status (Profile Header ke liye)
    fun getRelationshipInfo(targetUserId: Long, currentUserId: Long): RelationshipInfoResponse {
        val followers = followRepository.countFollowers(targetUserId)
        val following = followRepository.countFollowing(targetUserId)

        // Check: Kya main (currentUser) is targetUser ko follow karta hu?
        val isFollowing = followRepository.isFollowing(currentUserId, targetUserId)

        return RelationshipInfoResponse(
            followersCount = followers,
            followingCount = following,
            isFollowing = isFollowing
        )
    }

    fun getProfileStats(targetUserId: Long, currentUserId: Long): ProfileStatsResponse {

        // 1. Parallel Queries (DB optimizations baad me, abhi simple rakho)
        val posts = postRepository.countByAuthorId(targetUserId)
        val followers = followRepository.countFollowers(targetUserId)
        val following = followRepository.countFollowing(targetUserId)

        // 2. Check Relationship (Main isse follow karta hu ya nahi?)
        // Agar main khud apni profile dekh raha hu, to false return hoga (UI handle karega)
        val isFollowing = if (targetUserId == currentUserId) {
            false
        } else {
            followRepository.isFollowing(currentUserId, targetUserId)
        }

        return ProfileStatsResponse(
            postCount = posts,
            followersCount = followers,
            followingCount = following,
            isFollowing = isFollowing
        )
    }
}