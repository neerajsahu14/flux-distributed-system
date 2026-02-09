package com.neerajsahu.flux.server.common.exception

/**
 * Custom exceptions for the application.
 * Provides more specific error handling than generic RuntimeException.
 */

// Base exception for all application exceptions
open class FluxException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

// Resource not found exceptions
open class ResourceNotFoundException(
    resourceType: String,
    identifier: Any
) : FluxException("$resourceType not found with identifier: $identifier")

class PostNotFoundException(postId: Long) : ResourceNotFoundException("Post", postId)
class UserNotFoundException(userId: Long) : ResourceNotFoundException("User", userId)

// Authorization exceptions
class UnauthorizedException(message: String) : FluxException(message)
class ForbiddenException(message: String) : FluxException(message)

// Validation exceptions
class ValidationException(message: String) : FluxException(message)
class DuplicateResourceException(message: String) : FluxException(message)

