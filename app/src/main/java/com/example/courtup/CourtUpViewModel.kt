package com.example.courtup

import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ApiConfig {
    // Emulator: 10.0.2.2; physical phone: replace with your computer's LAN IP.
    const val BASE_URL = "http://10.0.2.2:5000/"
}

data class Room(val id: Int, val sport: String, val location: String, val time: String,
    val players: Int, val maxPlayers: Int, val host: String,
    val members: List<String>, val pending: List<String>) {
    fun state(name: String) = when {
        host == name -> "host"
        name in members -> "member"
        name in pending -> "pending"
        players >= maxPlayers -> "full"
        else -> "join"
    }
}

class RoomsApi {
    private val client = OkHttpClient.Builder().callTimeout(20, java.util.concurrent.TimeUnit.SECONDS).build()
    suspend fun call(path: String = "api/rooms", body: JSONObject? = null): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(ApiConfig.BASE_URL + path).apply {
            if (body != null) post(body.toString().toRequestBody("application/json".toMediaType()))
        }.build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException(
                runCatching { JSONObject(text).optString("error") }.getOrDefault("")
                    .ifBlank { "Request failed (HTTP ${response.code})" })
            text
        }
    }
    suspend fun rooms(): List<Room> {
        val array = JSONArray(call())
        return (0 until array.length()).map { i ->
            val r = array.getJSONObject(i)
            fun names(key: String): List<String> {
                val a = r.getJSONArray(key)
                return (0 until a.length()).map { a.getString(it) }
            }
            Room(r.getInt("id"), r.getString("sport"), r.getString("location"), r.getString("time"),
                r.getInt("players"), r.getInt("maxPlayers"), r.getString("host"), names("members"), names("pending"))
        }
    }
}

class CourtUpViewModel(private val saved: SavedStateHandle) : ViewModel() {
    var username by mutableStateOf(saved.get<String>("username").orEmpty()); private set
    var page by mutableStateOf(saved.get<String>("page") ?: "Home"); private set
    var rooms by mutableStateOf(emptyList<Room>()); private set
    var loading by mutableStateOf(false); private set
    var busy by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var message by mutableStateOf<String?>(null); private set
    var created by mutableStateOf(false); private set
    private val api = RoomsApi()
    init { if (username.isNotEmpty() && page == "Rooms") refresh() }
    fun enter(name: String) { if (name.trim().isNotEmpty()) { username = name.trim(); saved["username"] = username } }
    private val history = saved.get<ArrayList<String>>("history") ?: arrayListOf<String>()
    fun back() {
        if (busy) return
        val previous = if (history.isEmpty()) "Home" else history.removeAt(history.lastIndex)
        saved["history"] = history
        page = previous; saved["page"] = page; error = null; message = null
        if (page == "Rooms") refresh()
    }
    fun navigate(destination: String) {
        if (busy || destination == page) return
        history.add(page); saved["history"] = history
        page = destination; saved["page"] = destination; error = null; message = null
        if (destination == "Rooms") refresh()
    }
    fun dismissMessage() { message = null }
    fun refresh() {
        if (loading || busy) return
        viewModelScope.launch {
            loading = true; error = null
            try { rooms = api.rooms() }
            catch (e: Exception) { if (e is CancellationException) throw e; error = e.message ?: "Unable to load rooms" }
            finally { loading = false }
        }
    }
    fun create(sport: String, location: String, time: String, max: String) {
        if (busy || loading || created) return
        if (location.isBlank() || time.isBlank() || max.isBlank()) { error = "Fill all fields"; return }
        if ((max.toIntOrNull() ?: 0) < 1) { error = "Max Players must be a positive whole number"; return }
        perform {
            api.call(body = JSONObject().put("sport", sport).put("location", location).put("time", time)
                .put("maxPlayers", max).put("host", username))
            created = true
            delay(1500)
            history.removeAll { it == "Create" }
            saved["history"] = history
            page = "Rooms"; saved["page"] = page; created = false
        }
    }
    fun action(room: Room, action: String, person: String = "") {
        if (busy || loading) return
        perform {
            val body = JSONObject().put("username", username)
            if (action == "accept" || action == "reject") body.put("requester", person)
            if (action == "remove") body.put("target", person)
            api.call("api/rooms/${room.id}/$action", body)
            if (action == "request") message = "Join request sent! Waiting for host approval."
        }
    }
    private fun perform(block: suspend () -> Unit) {
        busy = true; error = null; message = null
        viewModelScope.launch {
            try {
                block()
                try { rooms = api.rooms() }
                catch (e: Exception) {
                    if (e is CancellationException) throw e
                    error = "Action succeeded, but rooms could not be refreshed. Tap Refresh."
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                error = e.message ?: "Request failed"
            } finally { busy = false }
        }
    }
}
