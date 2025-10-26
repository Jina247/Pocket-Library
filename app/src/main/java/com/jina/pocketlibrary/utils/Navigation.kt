package com.jina.pocketlibrary.utils

import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.navigation.NavController

fun NavController.navigateToDetail(bookId: String) {
    val encodedId = URLEncoder.encode(bookId, StandardCharsets.UTF_8.toString())
    this.navigate("detail/$encodedId")
}

fun decodeBookId(encodedId: String?): String? {
    return encodedId?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
}
