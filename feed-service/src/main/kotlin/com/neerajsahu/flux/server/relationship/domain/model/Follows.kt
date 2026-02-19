package com.neerajsahu.flux.server.relationship.domain.model

import com.neerajsahu.flux.server.auth.domain.model.User
import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

@Embeddable
data class FollowId(
    val followerId: Long = 0,
    val followeeId: Long = 0
) : Serializable


@Entity
@Table(name = "follows")
data class Follow(
    @EmbeddedId
    val id: FollowId = FollowId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id")
    val follower: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followeeId")
    @JoinColumn(name = "followee_id")
    val followee: User,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "isvalid")
    var isValid: Boolean = true
)