# Change Log

This project adheres to [Semantic Versioning](http://semver.org/).
       
See [here](http://keepachangelog.com/) for the change log format.

## [Unreleased]

Everything since what's listed below.

# akar v4.0.0 / 2025.12.28
- Upgraded to Clojure 1.12.0.

# akar-exceptions v4.0.0 / 2025.12.28
- Upgraded to Clojure 1.12.0.

# akar-core v4.0.0 / 2025.12.28
- Upgraded to Clojure 1.12.0.

# akar-commons v4.0.0 / 2025.12.28
- Upgraded to Clojure 1.12.0.

# akar v3.0.0 / 2024.07.29
- Upgraded to Clojure 1.11.3.

# akar-exceptions v3.0.0 / 2024.07.29
- Upgraded to Clojure 1.11.3.

# akar-core v3.0.0 / 2024.07.29
- Upgraded to Clojure 1.11.3.

# akar-commons v3.0.0 / 2024.07.28
- Upgraded to Clojure 1.11.3.

# akar v2.0.0 / 2018.04.13
- Umbrella project with the latest releases of satellite projects mentioned below.

# akar-exceptions v0.0.2 / 2018.04.13
- Initial release. (Accidentally missed v0.0.1.)

# akar-core v2.0.1 / 2018.04.13

### Changed
- `seqex` upgraded to 2.0.2.
- `akar-core` now depends on `akar-commons`.

# akar-commons v0.0.1 / 2018.04.12
- Initial release.

# akar-core v2.0.0 / 2018.04.11

### Changed
- The project `akar` is now called `akar-core`. `akar` will henceforth refer to the umbrella project housing all the satellite projects.
- `:constant` syntactic pattern is now more general. Earlier you could only match a symbol in a scope. Now it accepts any form whatsoever.
- `:type` no longer binds the value being matched. **This is a breaking change.** Replace all occurrences of `(:type SomeType some-symbol)` in your code with `(:and (:type SomeType) some-symbol)`.

### Added
- `!look-in` pattern function, and a corresponding `:look-in` syntactic pattern.

# akar v1.0.0 / 2018.03.23

### Changed
- Clojure version upgraded to 1.9.0

# akar v0.2.0 / 2017.11.06

### Added
- `if-match` construct
- `when-match` construct
- Syntactic support for non-literal constant patterns
- `defn-trampolined` - a utility to define trampolined recursive functions (by [@rahulkavale](https://github.com/rahulkavale)) 

# akar v0.1.0 / 2016.07.05

- Initial release
