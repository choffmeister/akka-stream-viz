# akka-stream-viz

[![Download](https://api.bintray.com/packages/choffmeister/maven/akka-stream-viz/images/download.svg)](https://bintray.com/choffmeister/maven/akka-stream-viz/_latestVersion)

## Usage

```
libraryDependencies += "de.choffmeister" %% "akka-stream-viz" % "..." // see download badge for latest version
```

## Development

```bash
# testing
sbt test
sbt scalafmt test:scalafmt
sbt scalafmtCheck test:scalafmtCheck

# packaging
sbt +publishLocal
sbt +publish

# documentation
sbt previewSite
sbt ghpagesPushSite
```
