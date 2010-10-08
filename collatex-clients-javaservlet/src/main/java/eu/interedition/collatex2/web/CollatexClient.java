package eu.interedition.collatex2.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

/**
 * Servlet implementation class Collatex
 */
public class CollatexClient extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public CollatexClient() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  @Override
  public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    String[] witnessInput = new String[7];
    for (int i = 0; i < witnessInput.length; i++) {
      witnessInput[i] = request.getParameter("text" + (i + 1));
    }
    final String outputType = request.getParameter("output_type");
    final String restService = request.getParameter("rest_service");

    final String jsonContent = createJson(witnessInput);

    final URL server = new URL(restService);
    final HttpURLConnection connection = (HttpURLConnection) server.openConnection();
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setUseCaches(false);
    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8;");
    connection.setRequestProperty("Accept", outputType);
    connection.setRequestMethod("POST");

    final Writer writer = new OutputStreamWriter(connection.getOutputStream());
    writer.write(jsonContent);
    //    System.out.println("content sent: " + jsonContent);
    writer.flush();
    writer.close();

    final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    response.setContentType(outputType);
    final PrintWriter servletOutput = response.getWriter();

    String line = null;
    while (null != (line = reader.readLine())) {
      servletOutput.println(line);
    }
    reader.close();
    servletOutput.close();

  }

  private static char BASE_ID = Character.valueOf('A').charValue();

  private String createJson(final String... witnesses) {
    final JSONArray jsonWitnesses = new JSONArray();
    for (int i = 0; i < witnesses.length; i++) {
      final String witness = witnesses[i];
      if (StringUtils.isNotEmpty(witness)) {
        final JSONObject jsonWitness = new JSONObject();
        jsonWitness.put("id", Character.valueOf((char) (BASE_ID + i)));
        jsonWitness.put("content", witness);
        jsonWitnesses.add(jsonWitness);
      }
    }
    final JSONObject object = new JSONObject();
    object.put("witnesses", jsonWitnesses);
    final String jsonContent = object.toString();
    return jsonContent;
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
    try {
      doGet(request, response);
    } catch (final IOException e) {
      e.printStackTrace();// handle the error here
    }
  }
}
