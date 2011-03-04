#
# CollateX - a Java library for collating textual sources,
# for example, to produce an apparatus.
#
# Copyright (C) 2010 ESF COST Action "Interedition".
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

curl -X POST --data-binary "{\"witnesses\":[{\"id\":\"A\",\"content\":\"A black cat in a black basket\" },{\"id\":\"B\", \"content\" : \"A black cat in a black basket\" },{\"id\" : \"C\", \"content\" : \"A striped cat in a black basket\" },{\"id\" : \"D\", \"content\" : \"A striped cat in a white basket\" }]}" -H"Content-Type: application/json;charset=UTF-8" -H"Accept: image/svg+xml" http://localhost:8080/collatex-web/api/collate > result.xml && firefox result.xml
