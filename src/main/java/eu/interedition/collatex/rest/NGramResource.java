package eu.interedition.collatex.rest;

import java.util.Arrays;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.collect.Lists;

import eu.interedition.collatex.experimental.ngrams.Alignment;
import eu.interedition.collatex.experimental.ngrams.NGram;
import eu.interedition.collatex.experimental.ngrams.WitnessSet;
import eu.interedition.collatex.experimental.ngrams.data.Witness;

public class NGramResource extends ServerResource {
  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };

  public NGramResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));
  }

  @Override
  public Representation get(final Variant variant) throws ResourceException {
    final List<String[]> useCases = useCases();
    final String[] firstUseCase = useCases.get(0);
    String html = "";
    html += "A: " + firstUseCase[0] + "</BR>";
    html += "B: " + firstUseCase[1] + "</BR>";

    final Witness a = new Witness("A", firstUseCase[0]);
    final Witness b = new Witness("B", firstUseCase[1]);
    final WitnessSet set = new WitnessSet(a, b);
    final Alignment align = set.align();
    final List<NGram> matches = align.getMatches();
    html += "</br>";
    html += "matches: ";
    for (final NGram nGram : matches) {
      html += " " + nGram.getNormalized() + ";";
    }
    final Representation representation = new StringRepresentation(html, MediaType.TEXT_HTML);
    return representation;
  }

  // NOTE: copied from UseCasePage!
  public List<String[]> useCases() {
    final List<String[]> usecases = Lists.newArrayList();
    usecases.add(new String[] { "The black cat", "The black and white cat", "The black and green cat", "The black very special cat", "The black not very special cat" });
    usecases.add(new String[] { "The black dog chases a red cat.", "A red cat chases the black dog.", "A red cat chases the yellow dog" });
    usecases.add(new String[] { "the black cat and the black mat", "the black dog and the black mat", "the black dog and the black mat" });
    usecases.add(new String[] { "the black cat on the table", "the black saw the black cat on the table", "the black saw the black cat on the table" });
    usecases.add(new String[] { "the black cat sat on the mat", "the cat sat on the black mat", "the cat sat on the black mat" });
    usecases.add(new String[] { "the black cat", "THE BLACK CAT", "The black cat", "The, black cat" });
    usecases.add(new String[] { "the white and black cat", "The black cat", "the black and white cat", "the black and green cat" });
    usecases.add(new String[] { "a cat or dog", "a cat and dog and", "a cat and dog and" });
    usecases.add(new String[] { "He was agast, so", "He was agast", "So he was agast", "He was so agast", "He was agast and feerd", "So was he agast" });
    usecases.add(new String[] { "the big bug had a big head", "the bug big had a big head", "the bug had a small head" });
    usecases.add(new String[] { "the big bug had a big head", "the bug had a small head" });
    usecases.add(new String[] { "the bug big had a big head", "the bug had a small head", "the bug had a small head" });
    usecases.add(new String[] { "the drought of march hath perced to the root and is this the right", "the first march of drought pierced to the root and this is the ",
        "the first march of drought hath perced to the root" });
    usecases.add(new String[] { "the drought of march hath perced to the root", "the march of the drought hath perced to the root", "the march of drought hath perced to the root" });
    usecases.add(new String[] { "the very first march of drought hath", "the drought of march hath", "the drought of march hath" });
    usecases.add(new String[] { "When April with his showers sweet with fruit The drought of March has pierced unto the root",
        "When showers sweet with April fruit The March of drought has pierced to the root", "When showers sweet with April fruit The drought of March has pierced the rood" });
    usecases.add(new String[] { "This Carpenter hadde wedded newe a wyf", "This Carpenter hadde wedded a newe wyf", "This Carpenter hadde newe wedded a wyf",
        "This Carpenter hadde wedded newly a wyf", "This Carpenter hadde E wedded newe a wyf", "This Carpenter hadde newli wedded a wyf", "This Carpenter hadde wedded a wyf" });
    usecases.add(new String[] { "Almost every aspect of what scholarly editors do may be changed",
        "Hardly any aspect of what stupid editors do in the privacy of their own home may be changed again and again",
        "very many aspects of what scholarly editors do in the livingrooms of their own home may not be changed" });
    usecases.add(new String[] { "Du kennst von Alters her meine Art, mich anzubauen, irgend mir an einem vertraulichen Orte ein Hüttchen aufzuschlagen, und da mit aller Einschränkung zu herbergen.",
        "Du kennst von Altersher meine Art, mich anzubauen, mir irgend an einem vertraulichen Ort ein Hüttchen aufzuschlagen, und da mit aller Einschränkung zu herbergen." });
    usecases.add(new String[] { "Auch hier hab ich wieder ein Plätzchen", "Ich hab auch hier wieder ein Pläzchen", "Ich hab auch hier wieder ein Pläzchen" });
    usecases.add(new String[] { "ταυτα ειπων ο ιϲ̅ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου των κεδρων οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου",
        "ταυτα ειπων ― ιϲ̅ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου των κεδρων οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου",
        "ταυτα ειπων ο ιη̅ϲ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου του κεδρου οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου" });
    usecases.add(new String[] { "I bought this glass, because it matches those dinner plates.", "I bought those glasses." });
    return usecases;
  }

}
