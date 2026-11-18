# ES Search Advanced Plugin

This plugin provides a Velocity viewtool (`$ESSearchUtils`) that runs a raw Elasticsearch query and returns the matching contentlets.

## Build

Requires Java 25 (the Java version dotCMS core runs on). Built against dotCMS core 26.11.x.

```sh
./mvnw package
```

The OSGi bundle is written to `target/com.dotcms.plugins.es-search-advanced-<version>.jar`. Upload it through the dotCMS Plugins portlet or drop it into the `felix/load` folder.

## Note

This plugin uses `APILocator.getEsSearchAPI()` and Elasticsearch client types, which dotCMS has deprecated as it moves its index to OpenSearch. It may need porting to the OpenSearch-based API in a future dotCMS release.
