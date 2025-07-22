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

enum class SoundQuality(
    override val key: String,
    override val description: String
) : SoundQualityDisplayable {
    STANDARD("standard", "标准"),
    HIGHER("higher", "较高"),
    EXHIGH("exhigh", "极高"),
    LOSSLESS("lossless", "无损"),
    HIRES("hires", "Hi-Res"),
    JYEFFECT("jyeffect", "高清环绕声"),
    SKY("sky", "沉浸环绕声"),
    DOLBY("dolby", "杜比全景声"),
    JYMASTER("jymaster", "超清母带");
}


interface SoundQualityDisplayable {
    val key: String
    val description: String
}


