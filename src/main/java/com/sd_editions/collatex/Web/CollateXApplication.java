package com.sd_editions.collatex.Web;

import org.apache.wicket.protocol.http.WebApplication;

public class CollateXApplication extends WebApplication {

  @Override
  public Class getHomePage() {
    return ColorsPage.class;
  }

}
