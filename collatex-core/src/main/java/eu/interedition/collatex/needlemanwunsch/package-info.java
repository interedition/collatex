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
 * A version of the Needleman-Wunsch algorithm.
 *
 * This <a href="http://en.wikipedia.org/wiki/Needleman%E2%80%93Wunsch_algorithm">algorithm</a> strives for global alignment of witnesses and bases the alignment on a configurable scoring of matches vs. differences/gaps.
 * It does not try to detect transpositions.
 *
 * @see eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm
 * @see eu.interedition.collatex.needlemanwunsch.NeedlemanWunschScorer
 */
package eu.interedition.collatex.needlemanwunsch;