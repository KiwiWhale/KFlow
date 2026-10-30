package io.github.mrjimin.kflow.context

class WorkflowContext {

    private val values = mutableMapOf<String, Any>()

    val size: Int
        get() = values.size

    val isEmpty: Boolean
        get() = values.isEmpty()

    val keys: Set<String>
        get() = values.keys.toSet()

    operator fun set(key: String, value: Any) {
        validateKey(key)
        values[key] = value
    }

    inline fun <reified T : Any> get(key: String): T? =
        getValue(key) as? T

    inline fun <reified T : Any> require(key: String): T {
        val value = getValue(key)
            ?: throw NoSuchElementException("Context value not found: $key")

        return value as? T
            ?: throw ClassCastException("Context value '$key' is not ${T::class.simpleName}")
    }

    fun contains(key: String): Boolean =
        values.containsKey(key)

    fun remove(key: String): Any? =
        values.remove(key)

    fun clear() {
        values.clear()
    }

    inline fun <reified T : Any> getOrDefault(
        key: String,
        defaultValue: T
    ): T = get<T>(key) ?: defaultValue

    inline fun <reified T : Any> getOrPut(
        key: String,
        defaultValue: () -> T
    ): T {
        val existing = getValue(key)

        if (existing != null) {
            return existing as? T
                ?: throw ClassCastException("Context value '$key' is not ${T::class.simpleName}")
        }

        return defaultValue().also {
            set(key, it)
        }
    }

    fun snapshot(): Map<String, Any> =
        values.toMap()

    @PublishedApi
    internal fun getValue(key: String): Any? =
        values[key]

    private fun validateKey(key: String) {
        require(key.isNotBlank()) {
            "Context key cannot be blank"
        }
    }
}