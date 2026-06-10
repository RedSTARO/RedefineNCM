package com.redstar.redefinencm.data.api.data

// Response of /cloudsearch — returns full song objects (same shape as SongDetailSongs)
data class SearchResult(
    val result: SearchResultData?,
    val code: Int,
)

data class SearchResultData(
    val songs: List<SongDetailSongs>?,
    val songCount: Int = 0,
)
