# pdfextract

A wrapper around [tabula](http://tabula.technology/) that turns PDFs into document trees. Useful for data scraping.

## Usage

```clojure

(require [pdfextract.core :as ex])

(def tree (ex/extract-content (java.io.File. "example.pdf"))) ;; extract-content takes anything that byte-streams can convert to an InputStream

;; value of tree:
({:min-x 140.4600067138672,
  :max-x 454.24000549316406,
  :min-y 58.7599983215332,
  :max-y 68.45999813079834,
  :text
  ([{:text "1.  PRODUCT AND COMPANY IDENTIFICATION",
     :direction 0.0,
     :space-width 3.8864403,
     :font {:font-family nil, :font-name nil, :font-stretch nil},
     :font-size 1.0}])}
 {:min-x 28.3799991607666,
  :max-x 445.4099979400635,
  :min-y 78.06999969482422,
  :max-y 85.01999950408936,
  :text
  ([{:text "Product Name: ",
     :direction 0.0,
     :space-width 2.7855604,
     :font {:font-family nil, :font-name nil, :font-stretch nil},
     :font-size 1.0}]
   [{:text
     "Klean Strip Adhesive Remover / Klean Strip Premium Stripper",
     :direction 0.0,
     :space-width 2.7855604,
     :font {:font-family nil, :font-name nil, :font-stretch nil},
     :font-size 1.0}])})

(def text-nodes (ex/text-tree tree))

;; value of text-nodes:
((("1.  PRODUCT AND COMPANY IDENTIFICATION"))
 (("Product Name: ")
  ("Klean Strip Adhesive Remover / Klean Strip Premium Stripper")))

```

## License

Copyright © 2017 Bob Poekert

Distributed under the MIT license.
