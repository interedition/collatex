/*
 * Copyright (c) 2015 The Interedition Development Group.
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

/**
 * Default implementations for core interfaces like witnesses and tokens.
 * <p/>
 * Classes in this package make fundamental assumptions about the nature of text version to be collated, e.g. that they
 * can be tokenized by whitespace, that tokens might be case insensitive, that punctuation might not matter or that
 * XML input adheres to a particular schema.
 * <p/>
 * Users are advised to implement {@link eu.interedition.collatex.Token} and {@link eu.interedition.collatex.Witness}
 * themselves and adjust their implementations to the use case at hand where those assumptions do not hold.
 */
package eu.interedition.collatex.simple;