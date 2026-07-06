package org.albaspazio.psysuite.core.exceptions

/**
 * Base exception for all PsySuite-specific errors.
 *
 * Serves as the root of the exception hierarchy, allowing callers to catch
 * all PsySuite exceptions with a single catch block if needed.
 */
open class PsySuiteException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when resource loading or access fails.
 *
 * This includes failures related to:
 * - Audio resources (MP3, WAV, etc.)
 * - Image resources
 * - Vibrator/hardware access
 * - Asset loading
 */
open class ResourceException(
    message: String,
    cause: Throwable? = null
) : PsySuiteException(message, cause)

/**
 * Exception thrown when audio resource loading fails.
 */
class AudioResourceException(
    message: String,
    cause: Throwable? = null
) : ResourceException(message, cause)

/**
 * Exception thrown when vibrator is not available or not defined.
 */
class VibratorNotDefinedException(
    message: String,
    cause: Throwable? = null
) : ResourceException(message, cause)

/**
 * Exception thrown when ImageView is not properly defined or configured.
 */
class ImageViewDefinedException(
    message: String,
    cause: Throwable? = null
) : ResourceException(message, cause)

/**
 * Exception thrown when configuration is invalid or malformed.
 *
 * This includes failures related to:
 * - Configuration node validation
 * - Configuration parsing
 * - Invalid configuration structure
 */
open class ConfigurationException(
    message: String,
    cause: Throwable? = null
) : PsySuiteException(message, cause)

/**
 * Exception thrown when string resolution fails.
 *
 * Occurs when a @string/ resource reference cannot be resolved.
 */
class StringResolutionException(
    message: String,
    cause: Throwable? = null
) : ConfigurationException(message, cause)

/**
 * Exception thrown when an object cannot be instantiated.
 *
 * This includes failures related to:
 * - Reflection-based instantiation
 * - Missing constructors
 * - Class loading failures
 * - Test instantiation
 */
open class PsyInstantiationException(
    message: String,
    cause: Throwable? = null
) : PsySuiteException(message, cause)

/**
 * Exception thrown when test instantiation fails.
 */
class TestInstantiationException(
    message: String,
    cause: Throwable? = null
) : PsyInstantiationException(message, cause)

/**
 * Exception thrown when state-related operation fails.
 *
 * This includes failures related to:
 * - Invalid state transitions
 * - Accessing properties in wrong state
 * - State invariant violations
 */
class StateException(
    message: String,
    cause: Throwable? = null
) : PsySuiteException(message, cause)

/**
 * Exception thrown when rendering operation fails.
 *
 * This includes failures related to:
 * - OpenGL/GLES operations
 * - Shader compilation
 * - Texture loading
 * - Surface operations
 */
class RenderException(
    message: String,
    cause: Throwable? = null
) : PsySuiteException(message, cause)

