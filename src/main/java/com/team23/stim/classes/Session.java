package com.team23.stim.classes;

import javax.servlet.http.HttpSession;

public class Session {
  HttpSession myQBOsession;
  Session(HttpSession session)
  {
    this.myQBOsession = session;
  }

  HttpSession getSession()
  {
    return this.myQBOsession;
  }
}
