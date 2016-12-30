# 1.2.0
- Fixed error in README re: dependency, thanks [danbills](https://github.com/danbills)
- Upgrade to json4s 3.5.0, this may cause some compat woes hence the version bum, thanks [dsiegmann-ssc](https://github.com/dsiegmann-ssc)

# 1.1.3

- Release fuckery.

# 1.1.0

Bugfixes:
- Actually shutdown the actor system on `shutdown` even it wasn't supplied via [#6](https://github.com/gphat/datadog-scala/pull/6), thanks [ddelatre](https://github.com/ddelautre)
- Fix misspelling in README via [#7](https://github.com/gphat/datadog-scala/pull/7), thanks [earldouglas](https://github.com/earldouglas)
- Fix missing imports in README vi [*8](https://github.com/gphat/datadog-scala/pull/8), thanks [earldouglas](https://github.com/earldouglas)

Features:
- Add `addServiceCheck` method
- Add `deleteEvent` method
- Add `query` method
