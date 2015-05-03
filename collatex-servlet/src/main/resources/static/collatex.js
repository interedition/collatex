/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

YUI.add('collatex', function(Y) {
    Y.CollateX = function(config) {
        config = (config || {});

        this.serviceUrl = (config.serviceUrl || "collate");
        this.algorithm = (config.algorithm || "dekker");
        this.tokenComparator = (config.tokenComparator || { type: "equality" });
        this.joined = (config.joined || true);
        this.transpositions = (config.transpositions || true);
    };
    Y.CollateX.prototype = {
        collate: function(resultType, witnesses, callback) {
            Y.io(this.serviceUrl, {
                method:"post",
                headers:{
                    "Content-Type":"application/json",
                    "Accept": resultType
                },
                data: Y.JSON.stringify({
                    witnesses: witnesses,
                    algorithm: this.algorithm,
                    tokenComparator: this.tokenComparator,
                    joined: this.joined,
                    transpositions: this.transpositions
                }),
                on:{
                    success: function(transactionId, resp) { callback(resp); },
                    failure: function(transactionId, resp) {
                        alert(resp.status + " " + resp.statusText);
                        Y.log(resp.status + " " + resp.statusText, "error", resp);
                    }
                }
            });
        },
        withDekker: function() {
            this.algorithm = "dekker";
            return this;
        },
        withNeedlemanWunsch: function() {
            this.algorithm = "needleman-wunsch";
            return this;
        },
        withMedite: function() {
            this.algorithm = "medite";
            return this;
        },
        withExactMatching: function() {
            this.tokenComparator = { type: "equality" };
            return this;
        },
        withFuzzyMatching: function(maxDistance) {
            this.tokenComparator = { type: "levenshtein", distance: maxDistance || 1 };
            return this;
        },
        toJSON: function(data, callback) {
            this.collate("application/json", data, function(resp) {
                callback(Y.JSON.parse(resp.responseText));
            });
        },
        toTEI : function(data, callback) {
            this.collate("application/tei+xml", data, function(resp) {
                callback(resp.responseText);
            });
        },
        toSVG: function(data, callback) {
            this.collate("image/svg+xml", data, function(resp) {
                callback(resp.responseXML.documentElement);
            });
        },
        toGraphViz: function(data, callback) {
            this.collate("text/plain", data, function(resp) {
                callback(resp.responseText);
            });
        },
        toGraphML: function(data, callback) {
            this.collate("application/graphml+xml", data, function(resp) {
                callback(resp.responseText);
            });
        },
        toTable: function(data, container) {
            this.toJSON(data, function(at) {
                var table = Y.Node.create('<table class="alignment"/>');
                var cells = [];
                var variantStatus = [];
                Y.each(at.table, function (r) {
                    var cellContents = [];
                    Y.each(r, function (c) {
                        cellContents.push(c.length == 0 ? null : Y.Array.reduce(c, "", function (str, next) {
                            next = Y.Lang.isString(next) ? next : Y.dump(next);
                            return str + next;
                        }));
                    });
                    cells.push(cellContents);
                    var cellContentsFiltered = Y.Array.filter(cellContents, function (c) {
                        return (c != null);
                    });
                    var cellContentsNormalized = Y.Array.map(cellContentsFiltered, function (c) {
                    	return Y.Lang.trimRight(c).toLowerCase();
                    });
                    variantStatus.push(Y.Array.dedupe(cellContentsNormalized).length == 1);
                });
                for (var wc = 0; wc < at.witnesses.length; wc++) {
                    var column = table.appendChild(Y.Node.create("<tr/>").append('<th>' + Y.Escape.html(at.witnesses[wc]) + '</th>'));
                    Y.each(cells, function (r, cc) {
                        var c = r[wc];
                        column.append('<td class="' + (variantStatus[cc] ? "invariant" : "variant") + (c == null ? " gap" : "") + '">' + (c == null ? "" : Y.Escape.html(c)));
                    });
                }

                container.append(table);
            });
        }
    };
}, "1", {
    requires: [ "node", "io", "json", "array-extras", "escape", "dump" ]
});
