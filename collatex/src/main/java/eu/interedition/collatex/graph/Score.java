/*
 * Copyright 2011 The Interedition Development Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.interedition.collatex.graph;

import java.util.Arrays;

/**
 * This class contains all the element needed for scoring an edge
 * There is a committed score
 * There is a temp score
 * There is a number of transitions between gap/no-gap
 * 
 * 
 * @author Ronald
 */
public class Score {
  public static final int BASEPROMOTIONFORMATCH = 2;
  public static final int PROMOTIONFORMATCH = 1; //TODO; work with doubles here?
  private final int[] score;

  public Score(int[] score) {
    this.score = score;
  }
  
  //TODO: 99: this should not be fixed like this
  public Score() {
    this(new int[] {99, 0, 0, 0, 0, 0});
  }

  public int getCommittedScore() {
    return score[0];
  }
  
  public int getNumberOfStateTransitions() {
    return score[1];
  }
  
  public int getTotalNumberOfEdgesInGaps() {
    return score[2];
  }
  
  public int getTotalNumberOfEdgesInMatches() {
    return score[3];
  }
  
  public int getNumberOfEdgesInGapsSinceTransition() {
    return score[4];
  }
  
  public int getNumberOfEdgesInMatchesSinceTransition() {
    return score[5];
  }
  
  public int getTempScore() {
    int tempScore = getCommittedScore();
    if (getNumberOfEdgesInMatchesSinceTransition()!=0) {
      tempScore -= BASEPROMOTIONFORMATCH + (getNumberOfEdgesInMatchesSinceTransition() * PROMOTIONFORMATCH);
    }
    return tempScore;
  }

  @Override
  public String toString() {
    return getTempScore()+":"+Arrays.toString(score);
  }

  
  int[] toArray() {
    return score;
  }

  Score copy() {
    return new Score(score.clone());
  }

  void addMatch() {
    this.score[3] += 1;
    this.score[5] += 1;
  }

  void addGap() {
    this.score[2] += 1;
    this.score[4] += 1;
  }

  void transitionState() {
    int tempScore = getTempScore();
    this.score[0] = tempScore;
    this.score[1] += 1;
    this.score[4] = 0;
    this.score[5] = 0;
  }

}