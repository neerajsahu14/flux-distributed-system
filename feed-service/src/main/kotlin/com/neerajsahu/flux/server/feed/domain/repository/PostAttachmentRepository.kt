package com.neerajsahu.flux.server.feed.domain.repository

import com.neerajsahu.flux.server.feed.domain.model.PostAttachment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostAttachmentRepository : JpaRepository<PostAttachment, Long>