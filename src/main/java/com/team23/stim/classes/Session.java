package com.team23.stim.classes;

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
