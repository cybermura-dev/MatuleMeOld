package ru.takeshiko.matuleme.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

class SupabaseClientManager
private constructor(private val supabaseClient: SupabaseClient) {

    val auth get() = supabaseClient.auth
    val storage get() = supabaseClient.storage
    val realtime get() = supabaseClient.realtime
    val postgrest get() = supabaseClient.postgrest

    companion object {
        @Volatile
        private var instance: SupabaseClientManager? = null

        fun initialize(url: String, key: String) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = SupabaseClientManager(
                            createSupabaseClient(url, key) {
                                install(Auth)
                                install(Storage)
                                install(Realtime)
                                install(Postgrest)
                            }
                        )
                    }
                }
            }
        }

        fun getInstance(): SupabaseClientManager {
            return instance ?: throw IllegalStateException("SupabaseClientManager not initialized!")
        }
    }
}