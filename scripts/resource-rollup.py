#!/usr/bin/env python

import re
import urllib
import sys

resource_pattern = re.compile(((len(sys.argv) > 1 and sys.argv[1] == "css") and "href" or "src") + "=\"([^\"]+)\"")

for url in resource_pattern.findall(sys.stdin.read()):
    sys.stdout.write(urllib.urlopen(url).read())
