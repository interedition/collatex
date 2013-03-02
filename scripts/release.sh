#!/bin/bash
#

shopt -s extglob
mvn -N clean install deploy
(cd collatex-core && mvn clean deploy && rsync -avz target/apidocs collatex:/var/www)
(cd collatex-cocoon && mvn clean deploy)
(cd collatex-tools && mvn clean package assembly:single && rsync -avz target/collatex-tools-*.@(tar.bz2|zip) collatex:/var/www/dist)
