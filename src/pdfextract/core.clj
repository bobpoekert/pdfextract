(ns pdfextract.core
  (:require [byte-streams :as bs])
  (:import [org.apache.pdfbox.pdmodel PDDocument PDPage]
           [org.apache.pdfbox.pdmodel.font PDFont PDTrueTypeFont PDFontDescriptorDictionary]
           [technology.tabula ObjectExtractor PageIterator Page TextElement TextChunk Line Rectangle TextElement]
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

(defn vec-butlast
  [v]
  (subvec v 0 (dec (count v))))

(defn font-merge
  [res v]
  (let [target (first res)]
    (if (and target
             (= (:font v) (:font target))
             (= (:font-size v) (:font-size target))
             (= (:bold? v) (:bold? target))
             (= (:italic? v) (:italic? target))
             (= (:plain? v) (:plain? target)))
      (conj (vec-butlast res) (assoc target :text (str (:text target) (:text v))))
      (conj res v))))

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
      :text (map #(reduce font-merge [] %) (map clojurize (.getTextElements this)))})
  TextChunk
  (clojurize [this]
    (map clojurize (.getTextElements this)))
  TextElement
  (clojurize [this]
    {
      :text (.getText this)
      :direction (.getDirection this)
      :space-width (.getWidthOfSpace this)
      :font (clojurize (.getFont this))
      :font-size (.getFontSize this)})
  PDFont
  (clojurize [this]
    (let [^PDFontDescriptorDictionary descriptor (.getFontDescriptor this)]
      {
        :font-family (.getFontFamily descriptor)
        :font-name (.getFontName descriptor)
        :font-stretch (.getFontStretch descriptor)})))

(defn extract-content
  "Takes a value that byte-streams can convert into an InputStream, parses it as a PDF, and returns a seq of pages, which are seqs of rectangle maps of the form
    :min-x :max-x :min-y :max-y :text (a seq of strings)"
  [pdf]
  (with-open [pdf (read-pdf pdf)]
    (doall
      (for [page (pages pdf)]
          (->>
            (.getTexts page)
            (TextElement/mergeWords)
            (TextChunk/groupByLines)
            (map clojurize))))))

(defn text-tree
  [extracted-content]
  (for [page extracted-content]
    (for [section page]
      (for [line (:text section)]
        (for [-chunk line]
          (:text -chunk))))))
