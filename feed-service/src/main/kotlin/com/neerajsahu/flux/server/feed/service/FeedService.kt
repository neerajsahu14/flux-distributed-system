package com.neerajsahu.flux.server.feed.service

import com.cloudinary.Cloudinary
import com.cloudinary.Transformation
import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.auth.service.AuthService
import com.neerajsahu.flux.server.common.exception.DuplicateResourceException
import com.neerajsahu.flux.server.common.exception.ForbiddenException
import com.neerajsahu.flux.server.common.mapper.PostMapper
import com.neerajsahu.flux.server.common.util.PostUtils
import com.neerajsahu.flux.server.feed.api.dto.*
import com.neerajsahu.flux.server.feed.domain.model.MediaType
import com.neerajsahu.flux.server.feed.domain.model.Post
import com.neerajsahu.flux.server.feed.domain.model.PostAttachment
import com.neerajsahu.flux.server.feed.domain.repository.PostAttachmentRepository
import com.neerajsahu.flux.server.feed.domain.repository.PostRepository
import com.neerajsahu.flux.server.interaction.service.InteractionService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

@Service
class FeedService(
    private val postRepository: PostRepository,
    private val attachmentRepository: PostAttachmentRepository,
    private val cloudinary: Cloudinary,
    private val authService: AuthService,
    private val interactionService: InteractionService,
    private val postMapper: PostMapper,
    private val postUtils: PostUtils
) {

    // ==================== CREATE ====================
    @Transactional
    fun createPost(user: User, file: MultipartFile, req: CreatePostRequest): PostResponse {
        // 1. Idempotency Check
        if (postRepository.existsByRequestId(req.requestId)) {
            throw DuplicateResourceException("Post with requestId ${req.requestId} already exists")
        }

        // 2. Save Post Metadata
        val post = Post(
            author = user,
            content = req.caption,
            requestId = req.requestId,
            attachmentCount = 1
        )
        val savedPost = postRepository.save(post)

        // 3. Detect File Type (Video or Image?)
        val contentType = file.contentType ?: "image/jpeg"
        val isVideo = contentType.startsWith("video")

        // Cloudinary Settings based on type
        val resourceType = if (isVideo) "video" else "image"

        val uploadParams = mapOf(
            "folder" to "flux-feed/posts",
            "resource_type" to resourceType
        )

        // 4. Upload File
        val uploadResult = cloudinary.uploader().upload(file.bytes, uploadParams)

        val publicId = uploadResult["public_id"] as String
        val originalUrl = uploadResult["secure_url"] as String

        // 5. Generate Smart Thumbnail
        val thumbnailUrl = if (isVideo) {
            cloudinary.url()
                .resourceType("video")
                .format("jpg")
                .transformation(Transformation<Transformation<*>>().width(500).crop("limit"))
                .generate(publicId)
        } else {
            cloudinary.url()
                .transformation(
                    Transformation<Transformation<*>>()
                        .width(500)
                        .crop("limit")
                        .quality("auto")
                        .fetchFormat("auto")
                )
                .generate(publicId)
        }

        // 6. Save Attachment
        val attachment = PostAttachment(
            post = savedPost,
            contentUrl = originalUrl,
            thumbnailUrl = thumbnailUrl,
            mediaType = if (isVideo) MediaType.VIDEO else MediaType.IMAGE
        )
        attachmentRepository.save(attachment)

        savedPost.attachments.add(attachment)

        return postMapper.toPostResponse(savedPost) { authService.getUserResponse(it) }
    }

    // ==================== READ ====================
    @Transactional(readOnly = true)
    fun getGlobalFeed(page: Int, size: Int): List<PostResponse> {
        val pageable = PageRequest.of(page, size)
        val postsPage = postRepository.findAllPosts(pageable)
        return postUtils.mapPageToPostResponses(postsPage) { post ->
            postMapper.toPostResponse(post) { authService.getUserResponse(it) }
        }
    }

    @Transactional(readOnly = true)
    fun getUserFeed(userId: Long, page: Int, size: Int): List<PostResponse> {
        val pageable = PageRequest.of(page, size)
        val postsPage = postRepository.findPostsByAuthorId(userId, pageable)
        return postUtils.mapPageToPostResponses(postsPage) { post ->
            postMapper.toPostResponse(post) { authService.getUserResponse(it) }
        }
    }

    @Transactional(readOnly = true)
    fun findPostByPostId(postId: Long): PostResponse {
        val post = postUtils.getValidPostOrThrow(postId)
        return postMapper.toPostResponse(post) { authService.getUserResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getPostDetail(postId: Long, user: User?): PostDetailResponse {
        val post = postUtils.getValidPostOrThrow(postId)

        val isLiked = user?.let { interactionService.isPostLikedByUser(it.id!!, post.id!!) } ?: false
        val isBookmarked = user?.let { interactionService.isPostBookmarkedByUser(it.id!!, post.id!!) } ?: false

        return postMapper.toPostDetailResponse(
            post = post,
            userMapper = { authService.getUserResponse(it) },
            isLiked = isLiked,
            isBookmarked = isBookmarked
        )
    }

    // ==================== UPDATE ====================
    @Transactional
    fun updatePost(user: User, postId: Long, req: UpdatePostRequest): PostResponse {
        val post = postUtils.getValidPostOrThrow(postId)

        // Authorization check
        if (post.author.id != user.id) {
            throw ForbiddenException("You can only update your own posts")
        }

        // Update fields
        post.content = req.caption
        post.updatedAt = Instant.now()

        val updatedPost = postRepository.save(post)
        return postMapper.toPostResponse(updatedPost) { authService.getUserResponse(it) }
    }

    // ==================== DELETE (Soft Delete) ====================
    @Transactional
    fun deletePost(user: User, postId: Long): Boolean {
        val post = postUtils.getValidPostOrThrow(postId)

        // Authorization check
        if (post.author.id != user.id) {
            throw ForbiddenException("You can only delete your own posts")
        }

        // Soft delete - set isValid to false
        post.isValid = false
        post.updatedAt = Instant.now()
        postRepository.save(post)

        return true
    }
}