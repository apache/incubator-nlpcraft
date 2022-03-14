package org.apache.nlpcraft.nlp.token.enricher.impl

import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.internal.util.NCUtils

import scala.collection.mutable

/**
  * Generates first word sequences.
  */
object NCEnStopWordGenerator:
    private final lazy val stemmer = new PorterStemmer

    // Output files.
    private val FIRST_WORDS_FILE = "first_words.txt"
    private val NOUN_WORDS_FILE = "noun_words.txt"

    private final val QWORDS = Seq(
        "what",
        "when",
        "where",
        "which",
        "who",
        "whom",
        "whose",
        "why",
        "how",
        "how much",
        "how many",
        "how long"
    )

    private final val DWORDS = Seq(
        "show",
        "list",
        "give",
        "display",
        "enumerate",
        "print",
        "tell",
        "say",
        "find",
        "select",
        "query",
        "count",
        "calculate",
        "produce",
        "chart",
        "draw",
        "plot",
        "get"
    )

    private final val QWORDS_SUP = Seq(
        "is",
        "are",
        "about"
    )

    private final val DWORDS_SUP = Seq(
        "me",
        "for me",
        "just for me",
        "us",
        "for us",
        "just for us",
        "for all of us",
        "just for all of us"
    )

    private final val WORDS2 = Seq(
        "please",
        "kindly",
        "simply",
        "basically",
        "just",
        "now"
    )

    private final val NOUN_WORDS = Seq(
        "data",
        "document",
        "info",
        "fact",
        "report",
        "result"
    )

    private final val NOUN_WORDS2 = Seq(
        "for",
        "about",
        "in",
        "around",
        "within",
        "in regards to",
        "with regards to",
        "related to",
        "specific to",
        "pertaining to",
        "in relation to",
        "correlated to",
        "specific for",
        "specifically about"
    )

    private final val DWORDS_PRE = Seq(
        "can you",
        "would you",
        "could you",
        "would you not",
        "could you not",
        "will you",
        "shall you",
        "how about you",
        "what about you",
        "if you",
        "if you can",
        "if you could",
        "what if you",
        "what if you can",
        "what if you could"
    )

    private final val QWORDS2 = Seq(
        "is there",
        "are there",
        "do we have",
        "do we not have",
        "do you have",
        "do you not have"
    )

    private final val QWORDS_ANY = Seq(
        "any",
        "some",
        "few",
        "several",
        "handful",
        "couple",
        "couple of"
    )

    private def mkGzip(path: String, lines: Iterable[Any]): Unit =
        val p = NCUtils.mkPath(s"nlpcraft/src/main/resources/stopwords/$path")
        NCUtils.mkTextFile(p, lines)
        NCUtils.gzipPath(p)

    private[impl] def mkNounWords(): Unit =
        val buf = new mutable.ArrayBuffer[String]()

        for (w1 <- NOUN_WORDS)
            buf += s"$w1"

        for (w1 <- NOUN_WORDS; w2 <- NOUN_WORDS2)
            buf += s"$w1 $w2"

        mkGzip(NOUN_WORDS_FILE, stem(buf.toSeq))

    private def stem(s: String): String = s.split(" ").map(p => stemmer.stem(p.toLowerCase)).mkString(" ")
    private def stem(seq: Seq[String]): Seq[String] = seq.map(stem)

    private[impl] def mkFirstWords(): Unit =
        val buf = new mutable.ArrayBuffer[String]()

        // is there
        for (w1 <- QWORDS2)
            buf += s"$w1"

        // please can you show what is
        for (w0 <- WORDS2; w1 <- DWORDS_PRE; w2 <- DWORDS; w3 <- QWORDS; w4 <- QWORDS_SUP)
            buf += s"$w0 $w1 $w2 $w3 $w4"

        // is there any
        for (w1 <- QWORDS2; w2 <- QWORDS_ANY)
            buf += s"$w1 $w2"

        // what is
        for (w1 <- QWORDS; w2 <- QWORDS_SUP)
            buf += s"$w1 $w2"

        // what
        for (w1 <- QWORDS)
            buf += s"$w1"

        // please what is
        for (w0 <- WORDS2; w1 <- QWORDS; w2 <- QWORDS_SUP)
            buf += s"$w0 $w1 $w2"

        // please what
        for (w0 <- WORDS2; w1 <- QWORDS)
            buf += s"$w0 $w1"

        // what is please
        for (w1 <- QWORDS; w2 <- QWORDS_SUP; w3 <- WORDS2)
            buf += s"$w1 $w2 $w3"

        // show me
        for (w1 <- DWORDS; w2 <- DWORDS_SUP)
            buf += s"$w1 $w2"

        // please show me
        for (w0 <- WORDS2; w1 <- DWORDS; w2 <- DWORDS_SUP)
            buf += s"$w0 $w1 $w2"

        // please show
        for (w0 <- WORDS2; w1 <- DWORDS)
            buf += s"$w0 $w1"

        // show me please
        for (w1 <- DWORDS; w2 <- DWORDS_SUP; w3 <- WORDS2)
            buf += s"$w1 $w2 $w3"

        // show please
        for (w1 <- DWORDS; w3 <- WORDS2)
            buf += s"$w1 $w3"

        // show
        for (w <- DWORDS)
            buf += s"$w"

        // can you please show me
        for (w0 <- DWORDS_PRE; w1 <- WORDS2; w2 <- DWORDS; w3 <- DWORDS_SUP)
            buf += s"$w0 $w1 $w2 $w3"

        // can you please show
        for (w0 <- DWORDS_PRE; w1 <- WORDS2; w2 <- DWORDS)
            buf += s"$w0 $w1 $w2"

        // please can you show me
        for (w0 <- WORDS2; w1 <- DWORDS_PRE; w2 <- DWORDS; w3 <- DWORDS_SUP)
            buf += s"$w0 $w1 $w2 $w3"

        // please can you show
        for (w0 <- WORDS2; w1 <- DWORDS_PRE; w2 <- DWORDS)
            buf += s"$w0 $w1 $w2"

        // can you show me
        for (w0 <- DWORDS_PRE; w2 <- DWORDS; w3 <- DWORDS_SUP)
            buf += s"$w0 $w2 $w3"

        // can you show
        for (w0 <- DWORDS_PRE; w2 <- DWORDS)
            buf += s"$w0 $w2"

        // can you please show what is
        for (w0 <- DWORDS_PRE; w1 <- WORDS2; w2 <- DWORDS; w3 <- QWORDS; w4 <- QWORDS_SUP)
            buf += s"$w0 $w1 $w2 $w3 $w4"

        // can you please
        for (w0 <- DWORDS_PRE; w1 <- WORDS2)
            buf += s"$w0 $w1"

        // can you please show what
        for (w0 <- DWORDS_PRE; w1 <- WORDS2; w2 <- DWORDS; w3 <- QWORDS)
            buf += s"$w0 $w1 $w2 $w3"

        // please can you show what
        for (w0 <- WORDS2; w1 <- DWORDS_PRE; w2 <- DWORDS; w3 <- QWORDS)
            buf += s"$w0 $w1 $w2 $w3"

        // can you show what is
        for (w0 <- DWORDS_PRE; w1 <- DWORDS; w3 <- QWORDS; w4 <- QWORDS_SUP)
            buf += s"$w0 $w1 $w3 $w4"

        // can you show what
        for (w0 <- DWORDS_PRE; w1 <- DWORDS; w3 <- QWORDS)
            buf += s"$w0 $w1 $w3"

        // can you please show me what is
        for (w0 <- DWORDS_PRE; w1 <- WORDS2; w2 <- DWORDS; w3 <- DWORDS_SUP; w4 <- QWORDS; w5 <- QWORDS_SUP)
            buf += s"$w0 $w1 $w2 $w3 $w4 $w5"

        // can you please show me what
        for (w0 <- DWORDS_PRE; w1 <- WORDS2; w2 <- DWORDS; w3 <- DWORDS_SUP; w4 <- QWORDS)
            buf += s"$w0 $w1 $w2 $w3 $w4"

        // please can you show me what is
        for (w0 <- WORDS2; w1 <- DWORDS_PRE; w2 <- DWORDS; w3 <- DWORDS_SUP; w4 <- QWORDS; w5 <- QWORDS_SUP)
            buf += s"$w0 $w1 $w2 $w3 $w4 $w5"

        // please can you show me what
        for (w0 <- WORDS2; w1 <- DWORDS_PRE; w2 <- DWORDS; w3 <- DWORDS_SUP; w4 <- QWORDS)
            buf += s"$w0 $w1 $w2 $w3 $w4"

        // can you show me what is
        for (w0 <- DWORDS_PRE; w1 <- DWORDS; w2 <- DWORDS_SUP; w3 <- QWORDS; w4 <- QWORDS_SUP)
            buf += s"$w0 $w1 $w2 $w3 $w4"

        // can you show me what
        for (w0 <- DWORDS_PRE; w1 <- DWORDS; w2 <- DWORDS_SUP; w3 <- QWORDS)
            buf += s"$w0 $w1 $w2 $w3"

        mkGzip(FIRST_WORDS_FILE, stem(buf.toSeq))

    /**
      *
      * @param args
      */
    def main(args: Array[String]): Unit =
        mkFirstWords()
        mkNounWords()

        sys.exit()
