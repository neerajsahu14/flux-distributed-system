package com.neerajsahu.flux.server.feed.domain.model

import jakarta.persistence.*

enum class MediaType { IMAGE, VIDEO }

@Entity
@Table(name = "post_attachments")
data class PostAttachment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // ERROR YAHAN THA: Default value missing thi
    // Fix: isse nullable banao aur default null do.
    // Hibernate reflection se isse baad me populate kar dega.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    val mediaType: MediaType = MediaType.IMAGE,

    @Column(name = "content_url", nullable = false)
    val contentUrl: String = "", // Default value added

    @Column(name = "thumbnail_url")
    val thumbnailUrl: String? = null,

    @Column(name = "display_order")
    val displayOrder: Int = 0,

    @Column(name = "isvalid")
    val isValid: Boolean = true
)