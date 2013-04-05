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

YUI().use("node", "event", "array-extras", function(Y) {
    Y.on("domready", function() {
        var toc = Y.one(".toc"), prettyprint = Y.one(".prettyprint");

        if (prettyprint) {
            prettyPrint();
        }

        if (toc) {
            var topLevel = [];
            Y.all("h2, h3, h4, h5, h6").each(function(heading) {
                var levelList = topLevel, level = parseInt(heading.get('tagName').substring(1)) - 2;
                for (var lc = 0; (levelList.length > 0) && (lc < level); lc++) {
                    levelList = levelList[levelList.length - 1].children;
                }
                levelList.push({ node: heading, title: heading.get("text"), children: [] });
            });

            var renderToc = function(node, headings, prefix) {
                Y.Array.each(headings, function(h, i) {
                    var ithPrefix = (prefix ? prefix + "." : "") + (i + 1),
                        title = ithPrefix + ". " + h.title,
                        anchor = h.node.get("id") || ("h" + ithPrefix.replace(".", "_"));

                    h.node.set("id", anchor);
                    h.node.set("text", title);

                    var li = node.appendChild(Y.Node.create("<li/>"));
                    li.appendChild(Y.Node.create("<a/>")).setAttrs({
                        text: title,
                        href: "#" + anchor
                    });

                    if (h.children.length > 0) {
                        renderToc(li, h.children, ithPrefix);
                    }
                });
                return node;
            };

            renderToc(toc, topLevel);
        }

        if (prettyprint || toc) {
            var hash = Y.getLocation().hash;
            if (hash) {
                var jumpTo = Y.one(hash);
                jumpTo && jumpTo.scrollIntoView(true);
            }
        }
    });
});