package com.juyoung.estherserver

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

val gson = Gson()

fun loadJsonResource(path: String): JsonObject {
    val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(path)
        ?: throw AssertionError("Resource not found: $path")
    return InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
        gson.fromJson(reader, JsonObject::class.java)
    }
}
