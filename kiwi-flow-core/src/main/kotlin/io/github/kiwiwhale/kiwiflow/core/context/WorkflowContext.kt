package io.github.kiwiwhale.kiwiflow.core.context

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * A thread-safe key-value storage associated with a workflow execution.
 *
 * WorkflowContext allows workflow steps to share additional data
 * independently of their input and output values.
 *
 * Values are stored using non-blank string keys and non-null objects.
 * The underlying storage is backed by [ConcurrentHashMap].
 *
 * Operations on individual keys are thread-safe. Compound operations
 * should use [getOrPut] or [update] when atomicity is required.
 *
 * Snapshots returned by [snapshot] are immutable shallow copies.
 *
 * Example:
 *
 * ```
 * val context = WorkflowContext()
 *
 * context["userId"] = "user-123"
 * context["attempts"] = 1
 *
 * val userId = context.require<String>("userId")
 *
 * context.update<Int>("attempts") { current ->
 *     (current ?: 0) + 1
 * }
 * ```
 *
 * @see KiwiFlowWorkflowScope
 */
class WorkflowContext {

    private val values = ConcurrentHashMap<String, Any>()

    /**
     * Number of entries currently stored in this context.
     *
     * The returned value represents a momentary observation and
     * may change immediately under concurrent modifications.
     */
    val size: Int
        get() = values.size

    /**
     * Whether this context currently contains no entries.
     *
     * This property represents a momentary observation.
     */
    val isEmpty: Boolean
        get() = values.isEmpty()

    /**
     * Returns an immutable snapshot of the currently stored keys.
     *
     * Subsequent modifications to the context are not reflected
     * in the returned set.
     */
    val keys: Set<String>
        get() = Collections.unmodifiableSet(values.keys.toSet())

    /**
     * Stores a non-null value under the specified key.
     *
     * Existing values associated with the key are replaced.
     *
     * @param key Non-blank context key.
     * @param value Non-null value to store.
     * @throws IllegalArgumentException If the key is blank.
     */
    operator fun set(key: String, value: Any) {
        validateKey(key)
        values[key] = value
    }

    /**
     * Retrieves a value and attempts to cast it to the requested type.
     *
     * Returns null when the key does not exist or the stored value
     * cannot be cast to [T].
     *
     * @param T Expected value type.
     * @param key Context key.
     * @return Stored value, or null if absent or incompatible.
     */
    inline operator fun <reified T : Any> get(key: String): T? =
        getValue(key) as? T

    /**
     * Retrieves a required value of the specified type.
     *
     * @param T Expected value type.
     * @param key Context key.
     * @return Stored value.
     * @throws NoSuchElementException If the key does not exist.
     * @throws ClassCastException If the stored value has an incompatible type.
     */
    inline fun <reified T : Any> require(key: String): T {
        val value = getValue(key)
            ?: throw NoSuchElementException(
                "Context value not found: '$key'"
            )

        return value as? T
            ?: throw ClassCastException(
                "Context value '$key' is not of type ${T::class.simpleName}"
            )
    }

    /**
     * Checks whether the specified key exists.
     *
     * @param key Context key.
     * @return True if the key exists, otherwise false.
     */
    fun contains(key: String): Boolean {
        validateKey(key)
        return values.containsKey(key)
    }

    /**
     * Removes the value associated with the specified key.
     *
     * @param key Context key.
     * @return Removed value, or null if the key did not exist.
     */
    fun remove(key: String): Any? {
        validateKey(key)
        return values.remove(key)
    }

    /**
     * Inserts a value only when the specified key is absent.
     *
     * This operation is atomic for the specified key.
     *
     * @param key Context key.
     * @param value Value to insert.
     * @return Existing value, or null if the new value was inserted.
     */
    fun putIfAbsent(key: String, value: Any): Any? {
        validateKey(key)
        return values.putIfAbsent(key, value)
    }

    /**
     * Removes all entries from this context.
     */
    fun clear() {
        values.clear()
    }

    /**
     * Returns the stored value or the supplied default value.
     *
     * @param T Expected value type.
     * @param key Context key.
     * @param defaultValue Value returned when the key is absent.
     * @return Stored value or default value.
     * @throws ClassCastException If an existing value has an incompatible type.
     */
    inline fun <reified T : Any> getOrDefault(
        key: String,
        defaultValue: T
    ): T {
        val value = getValue(key) ?: return defaultValue

        return value as? T
            ?: throw ClassCastException("Context value '$key' is not of type ${T::class.simpleName}")
    }

    /**
     * Retrieves the value associated with the specified key,
     * or atomically creates and stores a new value when absent.
     *
     * The [defaultValue] provider is evaluated only when the key
     * does not already exist.
     *
     * This operation is atomic for the specified key.
     * Concurrent calls for the same key will use the value
     * produced by the successful computation.
     *
     * If an existing value has an incompatible type,
     * a [ClassCastException] is thrown.
     *
     * @param T Expected value type.
     * @param key Context key.
     * @param defaultValue Provider used to create a value when absent.
     * @return Existing or newly created value.
     * @throws IllegalArgumentException If the key is blank.
     * @throws ClassCastException If the existing value has an incompatible type.
     *
     * @see getOrDefault
     * @see putIfAbsent
     */
    inline fun <reified T : Any> getOrPut(
        key: String,
        noinline defaultValue: () -> T
    ): T {
        val value = getOrPutValue(key, defaultValue)

        return value as? T
            ?: throw ClassCastException("Context value '$key' is not of type ${T::class.simpleName}")
    }

    /**
     * Atomically updates the value associated with the specified key.
     *
     * The current value is supplied to [transform], or null when
     * the key does not exist.
     *
     * The resulting value is stored under the same key.
     *
     * The transformation should not recursively modify the same
     * context key.
     *
     * @param T Expected value type.
     * @param key Context key.
     * @param transform Transformation applied to the current value.
     * @return Newly stored value.
     * @throws ClassCastException If the existing value has an incompatible type.
     */
    inline fun <reified T : Any> update(
        key: String,
        noinline transform: (T?) -> T
    ): T {
        val value = updateValue(key) { current ->
            val typed = current?.let {
                it as? T
                    ?: throw ClassCastException(
                        "Context value '$key' is not of type ${T::class.simpleName}"
                    )
            }

            transform(typed)
        }

        return value as T
    }

    /**
     * Creates an immutable shallow copy of the current context values.
     *
     * Changes to the context after this method returns do not affect
     * the returned map.
     *
     * Objects stored as values are not deeply copied.
     *
     * @return Immutable snapshot of the current entries.
     */
    fun snapshot(): Map<String, Any> =
        Collections.unmodifiableMap(HashMap(values))

    @PublishedApi
    internal fun getValue(key: String): Any? {
        validateKey(key)
        return values[key]
    }

    @PublishedApi
    internal fun <T : Any> getOrPutValue(
        key: String,
        defaultValue: () -> T
    ): Any {
        validateKey(key)

        return values.computeIfAbsent(key) {
            defaultValue()
        }
    }

    @PublishedApi
    internal fun updateValue(
        key: String,
        transform: (Any?) -> Any
    ): Any {
        validateKey(key)

        return values.compute(key) { _, current ->
            transform(current)
        }!!
    }

    private fun validateKey(key: String) {
        require(key.isNotBlank()) {
            "Context key cannot be blank"
        }
    }
}
