package com.neerajsahu.flux.server.feed.service

import com.cloudinary.Cloudinary
import com.cloudinary.Transformation
import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.auth.service.AuthService
import com.neerajsahu.flux.server.feed.api.dto.CreatePostRequest
import com.neerajsahu.flux.server.feed.api.dto.PostResponse
import com.neerajsahu.flux.server.feed.domain.model.MediaType
import com.neerajsahu.flux.server.feed.domain.model.Post
import com.neerajsahu.flux.server.feed.domain.model.PostAttachment
import com.neerajsahu.flux.server.feed.domain.repository.PostAttachmentRepository
import com.neerajsahu.flux.server.feed.domain.repository.PostRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class FeedService(
    private val postRepository: PostRepository,
    private val attachmentRepository: PostAttachmentRepository,
    private val cloudinary: Cloudinary,
    private val authService: AuthService
) {

    @Transactional
    fun createPost(user: User, file: MultipartFile, req: CreatePostRequest): PostResponse {
        // 1. Idempotency Check
        if (postRepository.existsByRequestId(req.requestId)) {
            throw RuntimeException("Duplicate request")
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
            "resource_type" to resourceType // Important: Tell Cloudinary it's a video
        )

        // 4. Upload File
        val uploadResult = cloudinary.uploader().upload(file.bytes, uploadParams)

        val publicId = uploadResult["public_id"] as String
        val originalUrl = uploadResult["secure_url"] as String

        // 5. Generate Smart Thumbnail
        val thumbnailUrl = if (isVideo) {
            // VIDEO MAGIC: Video ka 'public_id' use karke usse '.jpg' format maango.
            // Cloudinary automatic video ke beech se frame nikal ke dega.
            cloudinary.url()
                .resourceType("video") // Batana padta hai source video hai
                .format("jpg")         // Output humein image chahiye
                .transformation(Transformation<Transformation<*>>().width(500).crop("limit"))
                .generate(publicId)
        } else {
            // IMAGE LOGIC: Normal resizing
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
            contentUrl = originalUrl,    // Video URL (mp4) ya Image URL
            thumbnailUrl = thumbnailUrl, // Always an Image URL (jpg/webp)
            mediaType = if (isVideo) MediaType.VIDEO else MediaType.IMAGE
        )
        attachmentRepository.save(attachment)

        savedPost.attachments.add(attachment)

        return mapToResponse(savedPost)
    }

    fun getGlobalFeed(page: Int, size: Int): List<PostResponse> {
        val pageable = PageRequest.of(page, size)
        val postsPage = postRepository.findAllPosts(pageable)
        return postsPage.content.map { mapToResponse(it) }
    }

    private fun mapToResponse(post: Post): PostResponse {
        val attachment = if (post.attachments.isNotEmpty()) post.attachments[0] else null

        // Frontend Logic:
        // Agar Video hai, tab bhi hum 'thumbnailUrl' (Image) dikhayenge feed mein (Play button ke peeche).
        // Jab user click karega, tab 'contentUrl' (Video) play hoga.
        val displayImageUrl = attachment?.thumbnailUrl ?: attachment?.contentUrl ?: ""

        return PostResponse(
            id = post.id!!,
            caption = post.content,
            imageUrl = displayImageUrl,
            author = authService.getUserResponse(post.author),
            createdAt = post.createdAt.toString(),
            likeCount = post.likeCount
        )
    }
}