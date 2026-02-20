package com.neerajsahu.flux.server.interaction.domain.model

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.feed.domain.model.Post
import jakarta.persistence.*
import java.time.Instant

enum class ActionType { LIKED, SHARED, BOOKMARKED }
@Entity
@Table(
    name = "interactions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "post_id", "action_type"])]
)
data class Interaction(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post = Post(),

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    val actionType: ActionType = ActionType.LIKED,

    @Column(name = "request_id", unique = true, nullable = false)
    var requestId: String = "",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "isvalid")
    var isValid: Boolean = true
)