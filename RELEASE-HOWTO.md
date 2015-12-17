## Setup GPG

Releasing artifacts to Maven Central requires signing them. Make sure gpg is set up:

[http://central.sonatype.org/pages/working-with-pgp-signatures.html](http://central.sonatype.org/pages/working-with-pgp-signatures.html).

## Setup Maven

In `$HOME/.m2/settings.xml`, add credentials for accessing
[Sonatype's OSS Repository](https://oss.sonatype.org/):

    <settings>
      <servers>
        <server>
          <id>ossrh-interedition</id>
          <username>interedition</username>
          <password>...</password>
        </server>
      </servers>
    </settings>

## Update changelog

Edit `changelog.txt`.

## Update POMs and site to reflect new release version

    mvn versions:set -DnewVersion=1.2.3

Edit `site/grunt/jade.js`, update `dist.options.data.version` and regenerate site via

    grunt deploy

from the `site/` directory.

## Deploy artifacts to staging area

Activate the `release` profile in order to attach Javadocs and sources to build artifacts.

    mvn clean deploy -P release

## Commit and push released version to VCS and tag release

Push to Github and create a release.

## Release successfully staged artifacts

    mvn nexus-staging:release
