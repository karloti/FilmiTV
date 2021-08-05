package com.peshkunti.filmitv.classes

data class MySimpleDataClass(
    val httpStatusCode: Int,
    val httpStatusMessage: String,
    val paragraph: String,
    val allParagraphs: List<String>,
    val allLinks: List<String>,
)