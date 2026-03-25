package com.neerajsahu.flux.androidclient.feature.auth.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,

    val username: String,

    val email: String,

    // Nullable rakha hai kyunki naye user ke paas bio nahi hota
    val bio: String?,

    // ColumnInfo use karke database column ka naam clean rakha hai
    @ColumnInfo(name = "profile_pic_url")
    val profilePicUrl: String?
)