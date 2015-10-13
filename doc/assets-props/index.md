---
layout: index
title: assets-props
version: 0.11.2
---

# clean-css

Replace ```${expressions}``` with a value from ```application.conf```

## dependency

```xml
<dependency>
  <groupId>org.jooby</groupId>
  <artifactId>jooby-assets-props</artifactId>
  <version>0.11.2</version>
  <scope>test</scope>
</dependency>
```

## usage

```
assets {
 fileset {
   home: ...
 }
 pipeline {
   dev: [props]
   dist: [props]
 }
}
```

## options

```
assets {
 ...
 props {
   delims: [<%, %>]
 }
}
```