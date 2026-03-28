package com.neerajsahu.flux.server.relationship.service

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.auth.domain.repository.UserRepository
import com.neerajsahu.flux.server.feed.domain.repository.PostRepository
import com.neerajsahu.flux.server.relationship.api.dto.ProfileResponse
import com.neerajsahu.flux.server.relationship.api.dto.ProfileStatsResponse
import com.neerajsahu.flux.server.relationship.api.dto.RelationshipInfoResponse
import com.neerajsahu.flux.server.relationship.domain.model.Follow
import com.neerajsahu.flux.server.relationship.domain.model.FollowId
import com.neerajsahu.flux.server.relationship.domain.repository.FollowRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    fun getFollowers(userId: Long, currentUserId: Long, page: Int, size: Int): List<ProfileResponse> {
        val pageable = PageRequest.of(page, size)
        return followRepository.findFollowerProfilesWithStatus(userId, currentUserId, pageable).content
    }

    // 2. Get Following List
    fun getFollowing(userId: Long, currentUserId: Long, page: Int, size: Int): List<ProfileResponse> {
        val pageable = PageRequest.of(page, size)
        return followRepository.findFollowingProfilesWithStatus(userId, currentUserId, pageable).content
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
        val profile = followRepository.findProfileWithStatus(targetUserId, currentUserId)
            ?: throw RuntimeException("User not found")

        val posts = postRepository.countByAuthorId(targetUserId)
        val followers = followRepository.countFollowers(targetUserId)
        val following = followRepository.countFollowing(targetUserId)

        return ProfileStatsResponse(
            profile = profile,
            postCount = posts,
            followersCount = followers,
            followingCount = following,
            isFollowing = profile.isFollowing
        )
    }

    fun getCurrentUserProfileStats(currentUser: User): ProfileStatsResponse {
        return getProfileStats(currentUser.id!!, currentUser.id)
    }

    @Transactional(readOnly = true)
    fun getFollowersWithStatus(targetUserId: Long, currentUserId: Long, page: Int, size: Int): List<ProfileResponse> {
        val pageable = PageRequest.of(page, size)
        return followRepository.findFollowerProfilesWithStatus(targetUserId, currentUserId, pageable).content
    }

    @Transactional(readOnly = true)
    fun getFollowingWithStatus(targetUserId: Long, currentUserId: Long, page: Int, size: Int): List<ProfileResponse> {
        val pageable = PageRequest.of(page, size)
        return followRepository.findFollowingProfilesWithStatus(targetUserId, currentUserId, pageable).content
    }

    @Transactional(readOnly = true)
    fun searchGlobalUsers(query: String, currentUserId: Long, page: Int, size: Int): List<ProfileResponse> {
        if (query.isBlank()) {
            return emptyList()
        }

        val pageable = PageRequest.of(page, size)
        return followRepository.searchProfilesWithStatus(query, currentUserId, pageable).content
    }
}
