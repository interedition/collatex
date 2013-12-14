#!/bin/bash
#

# 1.) call mvn release:prepare beforehand in order to tag release
# 2.) checkout the tagged revision then and run this script in order to deploy
shopt -s extglob
mvn -N -Psonatype-oss-release clean install deploy
(cd collatex-core && mvn -Psonatype-oss-release clean deploy && rsync -avz target/apidocs collatex:htdocs)
(cd collatex-cocoon && mvn -Psonatype-oss-release clean deploy)
(cd collatex-tools && mvn clean package assembly:single && rsync -avz target/collatex-tools-*.@(tar.bz2|zip) collatex:htdocs/dist)
