# Akar Tutorial

## Pattern matching

In their report ["Pattern Matching for an Object-oriented and Dynamically Typed Programming Language"](https://publishup.uni-potsdam.de/files/4204/tbhpi36.pdf), Geller et al introduce the concept of pattern matching so (paraphrased):

> Pattern matching facilities were [first developed](http://comjnl.oxfordjournals.org/content/12/1/41.full.pdf) for functional programming languages. Today it is a well established feature and is part of mature languages such as Haskell or members of [the ML family](http://www.amazon.com/exec/obidos/ASIN/0262631326/acmorg-20). The original work on pattern matching describes syntactic extensions to a functional programming language that facilitate the definition of programs by means of structural induction. Supporting such a technique coincides with the mathematical approach of functional programming languages and the aim to promote equational reasoning. 

Pattern matching can be thought of as `if` on steroids. It allows you to decompose the data, inspect it for the desired structure or properties, and if affirmative, extract the relevant pieces. Patterns can be arbitrarily nested, allowing for deep data deconstruction. Quoting Geller et al again:

> In contrast to regular accessors and conditional statements, it can be argued that deep pattern matching allows concise, read-able deconstruction of complex data structures. More specifically, multiple nested conditional expressions quickly become difficult to read, while nested patterns allow destructuring of nested data containers in a single expression.



*sicp example*

