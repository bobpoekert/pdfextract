(ns pdfextract.core
  (:require [byte-streams :as bs])
  (:import [org.apache.pdfbox.pdmodel PDDocument PDPage]
           [technology.tabula ObjectExtractor PageIterator Page TextElement TextChunk Line Rectangle]
           [technology.tabula.detectors NurminenDetectionAlgorithm]))

(defn read-pdf
  "Takes a value that byte-streams can convert into an InputStream, parses it as a PDF, and returns a PDDocument"
  [blob]
  (PDDocument/load (bs/to-input-stream blob)))

(defn pages
  "returns a seq of tabula Page objects from a PDDocument"
  [^PDDocument inp]
  (iterator-seq (.extract (ObjectExtractor. inp))))

(defn content-rectangles
  [^Page page]
  (for [rect (.detect (NurminenDetectionAlgorithm.) page)]
    (.getArea page rect)))

(defprotocol Clojurable
  (clojurize [this]))

(extend-protocol Clojurable
  Rectangle
  (clojurize [this]
    {
      :min-x (.getMinX this)
      :max-x (.getMaxX this)
      :min-y (.getMinY this)
      :max-y (.getMaxY this)
      :text (map clojurize (.getTextElements this))})
  TextChunk
  (clojurize [this]
    (.getText this)))

(defn extract-content
  "Takes a value that byte-streams can convert into an InputStream, parses it as a PDF, and returns a seq of pages, which are seqs of rectangle maps of the form
    :min-x :max-x :min-y :max-y :text (a seq of strings)"
  [pdf]
  (with-open [pdf (read-pdf pdf)]
    (doall
      (for [page (pages pdf)
            rect (content-rectangles page)]
          (->>
            (.getTexts rect)
            (TextElement/mergeWords)
            (TextChunk/groupByLines)
            (map clojurize))))))
