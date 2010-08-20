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
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // TODO Auto-generated method stub
    String text1 = request.getParameter("text1").toString();
    String text2 = request.getParameter("text2").toString();
    String text3 = request.getParameter("text3").toString();
    String text4 = request.getParameter("text4").toString();
    String outputType = request.getParameter("output_type").toString();
    String restService = request.getParameter("rest_service").toString();

    String content = "{" + "\"witnesses\" : [ " + "{\"id\" : \"A\", \"content\" : \"" + text1 + "\" }, " + "{\"id\" : \"B\", \"content\" : \"" + text2 + "\" }, " + "{\"id\" : \"C\", \"content\" : \""
        + text3 + "\" }, " + "{\"id\" : \"D\", \"content\" : \"" + text4 + "\" } " + "]" + "}";

    URL server = new URL(restService);
    HttpURLConnection connection = (HttpURLConnection) server.openConnection();
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setUseCaches(false);
    connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8;");
    connection.setRequestProperty("Accept", outputType);
    connection.setRequestMethod("POST");

    Writer writer = new OutputStreamWriter(connection.getOutputStream());
    writer.write(content);
    writer.flush();
    writer.close();

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    response.setContentType(outputType);
    PrintWriter servletOutput = response.getWriter();

    String line = null;
    while (null != (line = reader.readLine())) {
      servletOutput.println(line);
    }
    reader.close();
    servletOutput.close();

  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    try {
      doGet(request, response);
    } catch (IOException e) {
      e.printStackTrace();// handle the error here
    }
  }
}
