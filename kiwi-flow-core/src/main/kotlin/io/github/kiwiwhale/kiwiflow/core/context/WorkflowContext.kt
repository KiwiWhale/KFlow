package io.github.kiwiwhale.kiwiflow.core.context

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class WorkflowContext {

    private val values = ConcurrentHashMap<String, Any>()

    val size: Int
        get() = values.size

    val isEmpty: Boolean
        get() = values.isEmpty()

    val keys: Set<String>
        get() = Collections.unmodifiableSet(values.keys.toSet())

    operator fun set(key: String, value: Any) {
        validateKey(key)
        values[key] = value
    }

    inline operator fun <reified T : Any> get(key: String): T? =
        getValue(key) as? T

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

    fun contains(key: String): Boolean {
        validateKey(key)
        return values.containsKey(key)
    }

    fun remove(key: String): Any? {
        validateKey(key)
        return values.remove(key)
    }

    fun putIfAbsent(key: String, value: Any): Any? {
        validateKey(key)
        return values.putIfAbsent(key, value)
    }

    fun clear() {
        values.clear()
    }

    inline fun <reified T : Any> getOrDefault(
        key: String,
        defaultValue: T
    ): T = get<T>(key) ?: defaultValue

    inline fun <reified T : Any> getOrPut(
        key: String,
        noinline defaultValue: () -> T
    ): T {
        val value = getOrPutValue(key, defaultValue)

        return value as? T
            ?: throw ClassCastException(
                "Context value '$key' is not of type ${T::class.simpleName}"
            )
    }

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
