package com.redstar.redefinencm.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.redstar.redefinencm.api.data.UserAccountData

@Entity(tableName = "userAccount")
data class UserAccountEntity(
    @PrimaryKey val userId: Long,
    val code: Int,
    val account: UserAccountData,
)