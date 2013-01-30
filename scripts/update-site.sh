#!/bin/bash
#
rsync -avz site/ collatex:/var/www

#(cd collatex-core && mvn javadoc:javadoc)
#rsync -avz collatex-core/target/site/apidocs collatex:/var/www