package com.redstar.redefinencm.util

object SettingProvider {
    lateinit var cookie: String
    lateinit var onlinePlayQuality: String
    lateinit var downloadQuality: String
    var statusBarLyric = false
    var replacePlaylist = false
    var checkUpdate = false

    fun exportAppSetting(){
        // TODO
    }

    fun importAppSetting(){
        // TODO
    }

}

enum class SoundQuality(private val displayName: String) {
    STANDARD("标准"),
    HIGHER("较高"),
    EXHIGH("极高"),
    LOSSLESS("无损"),
    HIRES("Hi-Res"),
    JYEFFECT("高清环绕声"),
    SKY("沉浸环绕声"),
    DOLBY("杜比全景声"),
    JYMASTER("超清母带");

    override fun toString(): String = displayName
}




interface SoundQualityDisplayable {
    val key: String
    val description: String
}


