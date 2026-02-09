package com.neerajsahu.flux.server.feed.domain.model

import com.neerajsahu.flux.server.auth.domain.model.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "posts")
data class Post(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val author: User = User(),

    @Column(columnDefinition = "TEXT")
    var content: String? = null,

    @Column(name = "request_id", unique = true, nullable = false)
    val requestId: String = "",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),

    @Column(name = "like_count")
    var likeCount: Int = 0,

    @Column(name = "share_count")
    var shareCount: Int = 0,

    @Column(name = "attachment_count")
    var attachmentCount: Int = 0,

    @Column(name = "isvalid")
    var isValid: Boolean = true,

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var attachments: MutableList<PostAttachment> = mutableListOf()
)