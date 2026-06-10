package com.redstar.redefinencm.data.api.data

// Response of /search/suggest?type=mobile — keyword predictions shown while typing
data class SearchSuggest(
    val result: SearchSuggestData?,
    val code: Int,
)

data class SearchSuggestData(
    val allMatch: List<SearchSuggestMatch>?,
)

data class SearchSuggestMatch(
    val keyword: String,
)
