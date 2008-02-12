package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.IntBlockVisitor;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class TextAlignmentVisitor implements IntBlockVisitor {
		private Block other;
		
		public TextAlignmentVisitor(BlockStructure variant) {
			this.other = variant.getRootBlock();
		}

		public TextAlignmentVisitor(Word variant) {
			this.other = variant;
		}

		public void visitBlockStructure(BlockStructure blockStructure) {
			Block rootBlock = blockStructure.getRootBlock();
			rootBlock.accept(this);
		}

		public void visitLine(Line line) {
			this.other = other.getFirstChild();
			Word w = (Word) line.getFirstChild();
			w.accept(this);
			while (w.hasNextSibling()) {
				w = (Word) w.getNextSibling();
				w.accept(this);
			}
			// TODO: move to next line etc...
		}

		public void visitWord(Word word) {
			Word the_other = (Word) other;
			if (word.aligns(the_other)) {
				the_other.setAlignedWord(word);
				this.other = other.getNextSibling();
			} else if (word.hasNextSibling() && the_other.hasNextSibling()){
				Word nextWord = (Word) word.getNextSibling();
				Word nextOtherWord = (Word) the_other.getNextSibling();
				if (nextWord.aligns(nextOtherWord)) {
					the_other.setAlignedWord(word);
					this.other=other.getNextSibling();
				}
			} else if ( !word.hasNextSibling() && the_other.hasNextSibling() ){
				Word nextOtherWord = (Word) the_other.getNextSibling();
				if (word.aligns(nextOtherWord)) {
					nextOtherWord.setAlignedWord(word);
					this.other=nextOtherWord.getNextSibling();
				}
				
			}
			
		}


}
