package com.xebia.functional.textsplitters

import com.xebia.functional.domain.Document
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CharacterTextSplitterSpec : StringSpec({
    "should return a list of strings after split with a given separator" {

        val text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."

        val separator = ", "
        val textSplitter = CharacterTextSplitter(separator)

        textSplitter.splitText(text) shouldBe listOf(
            "Lorem ipsum dolor sit amet",
            "consectetur adipiscing elit."
        )
    }

    "should return a list of documents after split on a list of documents with a given separator" {

        val documents = listOf(
            Document("Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
            Document("Sed do eiusmod tempor incididunt, ut labore et dolore magna aliqua.")
        )

        val separator = ", "
        val textSplitter = CharacterTextSplitter(separator)

        textSplitter.splitDocuments(documents) shouldBe listOf(
            Document("Lorem ipsum dolor sit amet"),
            Document("consectetur adipiscing elit."),
            Document("Sed do eiusmod tempor incididunt"),
            Document("ut labore et dolore magna aliqua.")
        )
    }

    "should return a list of documents after split on a text with a given separator" {

        val text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, ut labore et dolore magna aliqua."

        val separator = ", "
        val textSplitter = CharacterTextSplitter(separator)

        textSplitter.splitTextInDocuments(text) shouldBe listOf(
            Document("Lorem ipsum dolor sit amet"),
            Document("consectetur adipiscing elit"),
            Document("ut labore et dolore magna aliqua.")
        )
    }
})
