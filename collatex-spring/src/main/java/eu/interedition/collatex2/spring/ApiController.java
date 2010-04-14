package eu.interedition.collatex2.spring;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.parallel_segmentation.AlignmentTableSegmentator;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

@Controller
@RequestMapping("/api/**")
public class ApiController {
  private static final String WITNESS_1 = "abcdefgh";
  private static final String WITNESS_2 = "defgxyz";
  
  private Factory collateXEngine = new Factory();
  
  @RequestMapping("collate")
  public void collate(ModelMap model) {
    final IWitness witness1 = collateXEngine.createWitness("A", WITNESS_1);
    final IWitness witness2 = collateXEngine.createWitness("B", WITNESS_2);
    final IAlignmentTable alignmentTable = collateXEngine.createAlignmentTable(Lists.newArrayList(witness1, witness2));
    AlignmentTableSegmentator.createParrallelSegmentationTable(alignmentTable);
    model.addAttribute("hello", "world");
  }
}
