# Akar

[![Clojars Project](https://img.shields.io/clojars/v/akar.svg)](https://clojars.org/akar)
[![Clojars Project](https://img.shields.io/clojars/v/akar/akar-core.svg)](https://clojars.org/akar/akar-core)
[![Clojars Project](https://img.shields.io/clojars/v/akar/akar-exceptions.svg)](https://clojars.org/akar/akar-exceptions)
[![Clojars Project](https://img.shields.io/clojars/v/akar/akar-commons.svg)](https://clojars.org/akar/akar-commons)

<img src="graphics/logo.png" width="300">

> \[Speech] <br/>
> Queen: Hmm... <br/>
> <br/> 
> *Who's been painting my roses red?* <br/>
> *Who's been painting my roses red?* <br/>
> <br/> 
> *Who dares to taint with vulgar paint* <br/>
> *the royal flower bed?* <br/>
> <br/> 
> *For painting my roses red* <br/>
> *someone will lose his head.* <br/>
> <br/> 
> \[Spoken] <br/>
> **Three:** Oh no, your majesty, please, it's all his fault! <br/>
> **Two:** Not me, Your Grace. The Ace! The Ace! <br/>
> **Queen:** You? <br/>
> **Ace:** No, Two! <br/>
> **Queen:** The Deuce, you say? <br/>
> **Two:** Not me, the Tres! <br/>
> **Queen:** That's enough! **Off with their heads!** <br/>

Akar is a [pattern matching](https://en.wikibooks.org/wiki/Haskell/Pattern_matching) library for Clojure, with focus on simplicity and abstraction. 

Akar patterns are first-class values (just functions, actually), that can be manipulated, composed, abstracted over, like any other values. In fact, this is exactly how various pattern operations, such as guards, alternation, and views are implemented in Akar.

The library also features a syntactic layer that makes common use cases convenient, but at the same time stays true to the first-class spirit of the core model. 

Akar (IPA: \[ɑkɑɾ], Devanagari: आकार) is a Sanskrit/Marathi word for shape. The logo is a [Saraswati](https://en.wikipedia.org/wiki/Saraswati) [Kolam](https://en.wikipedia.org/wiki/Kolam), a diagrammatic representation of Hindu goddess of wisdom.
 
To learn more, read the [Akar tutorial](TUTORIAL.md) and [FAQs](FAQs.md).
 
## Releases and Dependency Information

```clojure
# Specific Akar projects
[akar/akar-core "3.0.0"]

# All Akar projects
[akar "3.0.0"]
```
 
## Example

```clojure
(ns your.app
  (:require [akar.syntax :refer [match]]
            [clojure.data.xml :as xml]))

; Example borrowed from https://wiki.scala-lang.org/display/SYGN/Xml-pattern-matching
(defn italics [xml-node]
  (match xml-node
         {:tag :i :content (:seq [contents])} (println contents)
         {:tag :node :content nodes}          (doseq [child nodes] (italics child))
         :_                                   nil))

(def xml-doc
  (xml/parse (java.io.StringReader.
               "<node>
                  <node>This is <i>some</i> text content.
                    <node>This is <i>deeper</i> stuff.</node>
                  </node>
                  <node>I am some text.
                    <title>I am <i>a title</i>.</title>
                    This is a sentence with an <i>italicized</i> entry.
                  </node>
                </node>")))

(italics xml-doc)

; prints:
;   some
;   deeper
;   italicized
```
 
## Contributing

Do you wish to contribute to Akar? Splendid! Get started [here](CONTRIBUTING.md). 
 
## License

Copyright © 2024 Rahul Goma Phulore

Distributed under Apache License 2.0.
