<%@ page import="java.net.*, java.io.*, java.util.*,
                 javax.xml.parsers.ParserConfigurationException,
                 edu.berkeley.gcweb.GameDictionary" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%!
private GameDictionary gameDictionary;

/**
 * Initializes the resources used by this JSP. This function runs
 * before other code in the page.
 */
public void jspInit() {
  ServletContext context = getServletConfig().getServletContext();
  try {
    URL dictionaryFile = context.getResource(
      "/WEB-INF/" + context.getInitParameter("gameDictionary"));
    gameDictionary = new GameDictionary(dictionaryFile);
  } catch (ParserConfigurationException e) {
    throw new RuntimeException(e);
  } catch (IOException e) {
    throw new RuntimeException(e);
  }
}

public void terminate(String message) {
  throw new RuntimeException("Terminating: " + message);
}
%>
<%
ServletContext context = getServletConfig().getServletContext();
String internalName = request.getParameter("game");
String canonicalName = gameDictionary.getCanonicalName(internalName);

// Ensure that the puzzle is specified and registered by the
// dictionary servlet.
if ((internalName == null) || (canonicalName == null)) {
  terminate(String.format("internal name (%s) and canonical name (%s)",
                          internalName, canonicalName));
}

String uiName = gameDictionary.getUI(internalName);
// Determine the appropriate file extension (JSP or HTML).
String templateFile = "/ui/game/" + uiName;
String absoluteJsp = context.getRealPath(templateFile + ".jsp");
templateFile += new File(absoluteJsp).exists() ? ".jsp" : ".html";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="en-US"> 
  <head> 
  <title><%= canonicalName %> - Games - GamesmanWeb</title> 
  <!--[if lte IE 7]>
  <style type="text/css">
    html > body > .header > h1 { display: inline }
    #moves { position: relative; left: 3em }
  </style>
  <![endif]-->
  <link rel="stylesheet" href="styles/style.css">
  <link rel="stylesheet" href="game/styles/gcweb.css">
  <link rel="stylesheet" href="game/styles/<%= uiName %>.css">
  <link rel="shortcut icon" type="image/vnd.microsoft.icon" href="../favicon.ico">
  <script type="text/javascript" src="https://code.jquery.com/jquery-1.4.2.min.js"></script>
  <script type="text/javascript" src="game/js/gc-game.js"></script>
  <script type="text/javascript" src="game/js/<%= uiName %>.js"></script>
<% if (canonicalName.equals("Quarto")) { %>
    <script type="text/javascript" src="game/js/drawing.js"></script>
	<script type="text/javascript" src="game/js/excanvas.js"></script>
<% } %>
  <script type="text/javascript" src="game/js/vvh.js"></script>
  <script type="text/javascript">
  
    $(document).ready(function() {
      $("#moves").css("min-height", Math.max($("#moves").height(), $("#main").height()));
      if ($("#option-prediction:checked").length == 0) {
        $("#prediction").hide();
      }
      $("#option-prediction").click(function() {
        if ($("#option-prediction:checked").length > 0) {
          $("#prediction").slideDown(250);
        } else {
          $("#prediction").slideUp(250);
        }
      });
    });
  </script>
  </head> 
  <body> 
    <p id="timer">&nbsp;</p>
    <p id="nr-moves">&nbsp;</p>
  <div class="header">
    <a href="../">
    <img src="images/gamescrafters-logo.png" alt="GamesCrafters">
    <h1>GamesmanWeb</h1>
    </a>
  </div>
  <div id="container"> 
    <!-- game commands --> 
    <div class="nav">
    <ul>
      <li><a href="javascript:location.reload();">Change Options</a></li>
      <li><a href="javascript:location.reload();">Reset Game</a></li>
    </ul>
    <h2>Display Options</h2>
    <ul id="options">
      <li><label><input type="checkbox" id="option-move-values">
                 Move-values</label></li>
      <li><label><input type="checkbox" id="option-prediction">
                 Prediction</label></li>
      <li><label><input type="checkbox" id="option-move-value-history">
                 Move-Value History</label></li>
    </ul>
        <% if (internalName.equals("y")) { %>
<h2>Rules for Y:</h2>

Players take turns playing at any point on the board not yet filled.
The first player to build a chain of pieces which connects all 3 edges together wins the game.
A corner piece counts as both edges.

        <% } %>
    </div> 
    <div id="main">
      <h1 id="gameheader"><%= canonicalName %></h1>
      <div id="game">
        <jsp:include page="<%= templateFile %>" />
      </div>
    </div> 
    <!-- sidebar --> 
    <div id="moves" class="aside">
    <div id="moves-inside">
     <div id="move-value-key">
      <h1>Move-value Key</h1>
      <table>
      <tr>
        <td><img src="images/win.png" alt="Green"></td>
        <td><img src="images/tie.png" alt="Yellow"></td>
        <td><img src="images/lose.png" alt="Red"></td>
      </tr>
      <tr>
        <td>winning move</td><td>cause a tie</td><td>losing move</td>
      </tr>
      </table>
    </div>
    <div id="prediction">
      <h1>Prediction</h1>
      <span>Game not started</span>
    </div>
	
	<div id="move-value-history">
          <h1>Move-value History</h1>
<div id="history-graph-container">
              <div id="history-graph"><canvas id="history-graph-canvas" width="250" /></div>
          </div>
</div>
</div>
	
    </div> 
  </div> 
  <!-- site footer --> 
  <div class="footer">
    <ul>
    <li>
      &copy; 2008-<%= Calendar.getInstance().get(Calendar.YEAR) %> <a href="http://gamescrafters.berkeley.edu" rel="external">GamesCrafters</a> and UC Regents</li>
    </ul>
  </div> 
  </body> 
</html> 
