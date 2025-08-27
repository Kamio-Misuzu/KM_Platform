// ArxivParser.kt
package com.example.mforum.utils

import com.example.mforum.data.ArxivPaper
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object ArxivParser {
    fun parseXmlResponse(xml: String): List<ArxivPaper> {
        val papers = mutableListOf<ArxivPaper>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()

        parser.setInput(StringReader(xml))
        var eventType = parser.eventType
        var currentPaper: ArxivPaper? = null
        var currentTag = ""
        var authors = mutableListOf<String>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "entry" -> {
                            currentPaper = ArxivPaper("", "", emptyList(), "", "", "")
                            authors = mutableListOf()
                        }
                        "title", "summary", "published", "id" -> {
                            currentTag = parser.name
                        }
                        "name" -> {
                            if (parser.name == "name") {
                                currentTag = "author"
                            }
                        }
                        "link" -> {
                            val rel = parser.getAttributeValue(null, "rel")
                            val title = parser.getAttributeValue(null, "title")
                            if (rel == "related" && title == "pdf") {
                                val pdfUrl = parser.getAttributeValue(null, "href")
                                currentPaper = currentPaper?.copy(pdfUrl = pdfUrl ?: "")
                            }
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    when (currentTag) {
                        "title" -> {
                            val title = parser.text.replace("\n", "").trim()
                            currentPaper = currentPaper?.copy(title = title)
                        }
                        "summary" -> {
                            val summary = parser.text.replace("\n", "").trim()
                            currentPaper = currentPaper?.copy(abstract = summary)
                        }
                        "published" -> {
                            val date = parser.text
                            currentPaper = currentPaper?.copy(publishedDate = date)
                        }
                        "id" -> {
                            val id = parser.text
                            currentPaper = currentPaper?.copy(id = id)
                        }
                        "author" -> {
                            authors.add(parser.text)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "entry" -> {
                            currentPaper = currentPaper?.copy(authors = authors)
                            currentPaper?.let { papers.add(it) }
                            currentPaper = null
                        }
                        "title", "summary", "published", "id", "author" -> {
                            currentTag = ""
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return papers
    }
}