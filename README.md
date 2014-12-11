[![Build Status](https://travis-ci.org/gphat/datadog-scala.png?branch=master)](https://travis-ci.org/gphat/datadog-scala)

# Datadog-Scala

A Scala library for interacting with the Datadog API.

As of October 2014 this library covers all the methods in the [Datadog API Documentation](http://docs.datadoghq.com/api/).

# Example

```scala
val client = new Client(apiKey = "XXX", appKey = "XXX")
client.getAllTimeboards.foreach({ response =>
    println(respone.body)
})
```

# Using It

```
// And a the resolver
resolvers += "gphat" at "https://raw.github.com/gphat/mvn-repo/master/releases/"

// Add the Dep
libraryDependencies += "datadog-scala" %% "datadog-scala" % "1.0.0"
```
