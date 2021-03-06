/**
 * connections.js
 * Extends GamesCrafters Web interface for Connections board game
 * Requires jquery, gc-game.js
 */

var size;
var BLUE = 0; var RED = 1;
var colors = ['blue', 'red'];
var boardArray = new Array();
var TURN = 0;
var lastMove = null;
var nextMoves = null;
function nextTurn() { TURN = (TURN == 0) ? 1 : 0; }

function Connections(options) {
  Connections.prototype.constructor.call(this, options);
}
GCWeb.extend(Connections, GCWeb.Game);

Connections.NAME = 'connections';
Connections.DEFAULT_SIZE = 11;

Connections.prototype.constructor = function(config) {
  config = config || {};
  config.options = config.options || {};
  size = config.size || Connections.DEFAULT_SIZE;
  boardArray = new Array(size);
  this.generateBoard(size);
  GCWeb.Game.prototype.constructor.call(this, Connections.NAME, size, size, config);
  $('#board').show();
  this.addEventListener('nextvaluesreceived', this.handleNextValuesReceived);
  this.name = 'connections';
}

Connections.prototype.getDefaultBoardString = function() {
  var s = '';
  var dict = new Array();
  dict['b'] = 'X';
  dict['r'] = 'O';
  dict[' '] = '%20';
  for (var i = boardArray.length-2; i >= 0; i -= 2) for (var j = 1; j < boardArray.length-1; j+=2) {
	s += dict[boardArray[i][j]];
  }
  for (var i = boardArray.length-3; i > 1; i -= 2) for (var j = 2; j < boardArray.length-2; j+=2) {
	s += dict[boardArray[i][j]];
  }
  s += ';side=' + ((boardArray.length-1)/2) + ';';
  return s;
}

function getSquares() {
	squares = [];
	for (var i = boardArray.length-2; i >= 0; i -= 2) for (var j = 1; j < boardArray.length-1; j+=2) {
		squares.push(document.getElementById(i+'_'+j));
	 }
	  for (var i = boardArray.length-3; i > 1; i -= 2) for (var j = 2; j < boardArray.length-2; j+=2) {
		  squares.push(document.getElementById(i+'_'+j));
	 }
	return squares;
}

Connections.prototype.createParameterString = function(board) {
  if (board != null && board.length >= 10) return ";board=" + board + ';side=' + ((boardArray.length-1)/2) + ';';
  var paramString = ";board=" + Connections.prototype.getDefaultBoardString();
  return paramString;
};

Connections.prototype.doMove = function(moveDelta, moves) {
	var s = 'moves: '
		for (var i = 0; i < moves.length; i++) s += moves[i].move + ' ';
	//alert(s);
		//alert(moveDelta);
	  // Find the current move-value object that represents the specified move.
	  var moveValue = null;
	  for (var i = 0; i < moves.length; i++) {
	    if (moves[i].move == moveDelta) {
	      moveValue = moves[i];
	      lastMove = moves[i];
	      break;
	    }
	  }
	  this.getNextMoveValues(moveValue.board);
	  return true;
}

Connections.prototype.getLastMoveValue = function() {
	return lastMove;
};

GCWeb.Game.prototype.getLastMoveValue = function() {
	  return lastMove;
};


Connections.prototype.getNextMoveValues = function(board) {
	  var serverUrl = GCWeb.Game.serviceUrl + encodeURIComponent('connections') +
	    "/getNextMoveValues" + this.createParameterString(board);
	  var options = {dataType: "json", url: serverUrl};
	  options.success = function(data, textStatus, xhr) {
	    if (data.status == "ok") {
	      this.nextMoves = data.response;
	      nextMoves = data.response;
	      this.handleNextValuesReceived();
	    } else {
	      var message = data.message ? '\n[' + data.message  + ']' : '';
	      GCWeb.alert('The GamesCrafters server could not handle the request.' +
	                  message);
	      //this._clearDoMoveRequests();
	    }
	  }.bind(this);
	  options.error = function(xhr, textStatus, errorThrown) {
	    GCWeb.alert('The GamesCrafters server is not responding. [' + textStatus +
	                ': ' + errorThrown + ']');
	    //this._clearDoMoveRequests();
	  }.bind(this);
	  $.ajax(options);
	}

Connections.prototype.start = function(team) {
  var TURN = 0;
  this.player = team || GCWeb.Team.BLUE;
  this.switchTeams();
  Connections.superClass.start.call(this);
  $('#whoseTurn').html('<span class="gc-blue">BLUE</span> to play.')
}

Connections.prototype.displayPrediction = function() {
	var minWin = 100000;
	var minLose = -1;
	for (var i = 0; i < nextMoves.length; i++) {
	  if (nextMoves[i].value == 'win') {
	    if (nextMoves[i].remoteness < minWin) minWin = nextMoves[i].remoteness;
	  }
	}
	for (var i = 0; i < nextMoves.length; i++) {
	  if (nextMoves[i].value == 'lose') {
		  if (nextMoves[i].remoteness > minLose) minLose = nextMoves[i].remoteness;
	  }
	}
	if (minWin < 100000) $('#prediction > span').html('<span class="gc-'+colors[TURN]+'">'+colors[TURN].toUpperCase()+'</span> <span class="gc-win">wins</span> in ' + (minWin+1) + ((minWin+1>1) ? ' moves.' : ' move.'));
	else if (minLose > -1) $('#prediction > span').html('<span class="gc-'+colors[TURN]+'">'+colors[TURN].toUpperCase()+'</span> <span class="gc-lose">loses</span> in ' + (minLose+1) + ((minLose+1>1) ? ' moves.' : ' move.'));
	else $('#prediction > span').html('Prediction unavailable');
}

Connections.prototype.occupied = function(space) {
	var xpos = parseInt($(space).attr('id').split('_')[0]);
    var ypos = parseInt($(space).attr('id').split('_')[1]);
    if (boardArray[xpos][ypos] != ' ' ||
		(TURN == 1 && (xpos == 0 || xpos == size-1)) ||
    	(TURN == 0 && (ypos == 0 || ypos == size-1))) {
			return true;
	}
	return false;
}

Connections.prototype.assignMoves = function(moves) {
	var squares = $(getSquares()); //$($('#board .even .even, #board .odd .odd').get().reverse());
	squares.unbind('click', moveHandler);
	var squaresArray = squares.get();
	var count = squaresArray.length;
	var moveDeltas = new Array(count);
	for (var j = 0; j < moveDeltas.length; j++) moveDeltas[j] = j;
	var j = 0;
	while (j < squaresArray.length) {
		if (Connections.prototype.occupied(squaresArray[j])) {
			j++;
			continue;
		}
		var moveDelta = moveDeltas[j];
		$(squaresArray[j]).bind('click', {'moveDelta': moveDelta, 'moves': moves}, moveHandler);
		j++;
	}
}

var moveHandler = function(e) { Connections.prototype.doMove(e.data.moveDelta, e.data.moves.slice()); } 

Connections.prototype.showMoveValues = function(moves) {
	moves = nextMoves;
	// clear move values
	this.hideMoveValues();
	if ($('#option-move-values:checked').val() != null) {	
		var squares = $(getSquares()); //$($('#board .even .even, #board .odd .odd').get().reverse());
		var i = 0;
		var squaresArray = squares.get();
		for (var j = 0; j < squaresArray.length; j++) {
			if (this.occupied(squaresArray[j])) {
				//$(squaresArray[j]).addClass('occupied');
				continue;
			}
			
			$(squaresArray[j]).addClass(moves[i].value);
			i++;
		}
	}
	this.assignMoves(moves.slice());
}

Connections.prototype.hideMoveValues = function() {
	$('.win').removeClass('win');
	$('.lose').removeClass('lose');
	$('.tie').removeClass('tie');
}

Connections.prototype.handleNextValuesReceived = function() {
	//this.switchTeams();
	var msg = '';
	if (this.nextMoves.length == 0) {
		var prompt = colors[(TURN+1)%2].toUpperCase() + ' WINS!';
	      prompt += " Would you like to reset the game and play again?";
	      var onAccept = function() {
	    	TURN = 0;
	        var game = new Connections({size: $('#board-options select').val()*2-1});
	        game.start();
	      };
	      GCWeb.confirm(prompt, onAccept);
	      return;
	}
	for (var i = 0; i < this.nextMoves.length; i++) msg += this.nextMoves[i].value + '-';
	//alert(msg);
	this.showMoveValues(this.nextMoves.slice());
	this.displayPrediction();
	$('#whoseTurn').html('<span class="gc-' + colors[TURN] + '">' + colors[TURN].toUpperCase() + '</span> to play.')
}

Connections.prototype.generateBoard = function(size) {
  
  this.board = $(document.getElementById('board'));
  this.board.html('');

  // initialize board array
  boardArray = new Array(size);
  for (var i = 0; i < size; i++) boardArray[i] = new Array(size);
  for (var i = 0; i < size; i++) for (var j = 0; j < size; j++) {
	  // Since currently moving on outer spaces not allowed
	  if (i == 0 || i == size-1 || j == 0 || j == size-1) {
		  boardArray[i][j] = 'x';
		  continue;
	  }
	  boardArray[i][j] = (i%2==j%2) ? ' ' : ((i%2==0) ? 'b' : 'r');
  }
  boardArray[0][0] = boardArray[size-1][size-1] = boardArray[0][size-1] = boardArray[size-1][0] = 'x';

  // generate table structure
  for (var i = 0; i < size; i++) {
    var row = '<tr>';
    for (var j = 0; j < size; j++) {
      row += '<td id="' + i + '_' + j + '" class="' + ((j%2==0)?'even':'odd') + '"><div class="square"></div></td>';
    }
    row += '</tr>';
    this.board.append(row);
  }
  $('#board tr:even').addClass('even'); 
  $('#board tr:odd').addClass('odd');
  $('#board .even .odd .square').addClass('blue');
  $('#board .odd .even .square').addClass('red');
  
  // event handling
  
  function hoverIn() {
    if (Connections.prototype.occupied(this)) return;
    $(this).css('backgroundColor', (TURN==0)?'lightblue':'pink');
  }
  function hoverOut() {
    $(this).css('backgroundColor', null);
  }
  $('#board .odd .odd').hover(hoverIn, hoverOut);
  $('#board .even .even').hover(hoverIn, hoverOut);
  
  function clickFn() {
    // check that it's a valid move
    var xpos = parseInt($(this).attr('id').split('_')[0]);
    var ypos = parseInt($(this).attr('id').split('_')[1]);
    if (boardArray[xpos][ypos] != ' ' ||
      (TURN == 1 && (xpos == 0 || xpos == size-1)) ||
      (TURN == 0 && (ypos == 0 || ypos == size-1))) {
      return;
    }
    $(this).children().hide();
    // bridge horizontal: row even TURN 0, row odd TURN 1
    // bridge vertical: row even TURN 1, row ODD TURN 0
    if ($(this).parent().hasClass('even')) {
      if (TURN == 0) {
        $(this).children().addClass('bridgeHorizontal');
        boardArray[xpos][ypos] = '-';
      }
      else {
        $(this).children().addClass('bridgeVertical');
        boardArray[xpos][ypos] = '|';
      }
    }
    else {
      if (TURN == 1) {
        $(this).children().addClass('bridgeHorizontal');
        boardArray[xpos][ypos] = '-';
      }
      else {
        $(this).children().addClass('bridgeVertical');
        boardArray[xpos][ypos] = '|';
      }
    }
    $(this).children().addClass(colors[TURN]);
    $(this).children().show('fast');
    nextTurn();
  }
  $('#board .odd .odd').click(clickFn);
  $('#board .even .even').click(clickFn);
  
}

var encountered = ['',''];
function checkPrimitive() {
  encountered = ['',''];
  for (var i = 1; i < size; i+=2) {  
    if (checkConnected(0, i, RED)) {
      var prompt = 'RED WINS!';
      prompt += " Would you like to reset the game and play again?";
      var onAccept = function() {
        var game = new Connections({size: $('#board-options select').val()*2-1});
	game.start();
	game.showMoveValues();
      };
      GCWeb.confirm(prompt, onAccept);
    }
    else if (checkConnected(i, 0, BLUE)) {
      var prompt = 'BLUE WINS!';
      prompt += " Would you like to reset the game and play again?";
      var onAccept = function() {
        var game = new Connections({size: $('#board-options select').val()*2-1});
	game.start();
	game.showMoveValues();
      };
      GCWeb.confirm(prompt, onAccept);
    };
  }
}
function checkConnected(i, j, color) {
  var boardValue = boardArray[i][j];
  if (color == RED && boardValue == 'r');
  else if (color == BLUE && boardValue == 'b');
  else if (boardValue == '-' && ((j>0 && boardArray[i][j-1] == colors[color].charAt(0))||(j<size-1 && boardArray[i][j+1]==colors[color].charAt(0))));
  else if (boardValue == '|' && ((i>0 && boardArray[i-1][j] == colors[color].charAt(0))||(i<size-1 && boardArray[i+1][j]==colors[color].charAt(0))));
  else return false;
  if (encountered[color].indexOf(' '+i+'_'+j+' ') != -1) return false;
  encountered[color] += ' '+i+'_'+j+' ';
  if (color == RED && i == size - 1) return true;
  if (color == BLUE && j == size - 1) return true;
  if (j != 0) if (checkConnected(i,j-1,color)) return true;
  if (j != size-1) if (checkConnected(i,j+1,color)) return true;
  if (i != 0) if (checkConnected(i-1,j,color)) return true;
  if (i != size-1) if (checkConnected(i+1,j,color)) return true;
  return false;
}

function displayBoard() {
	var s='';
	for (var i = 0; i < size; i++) {
		for (var j = 0; j < size; j++) {
			s += boardArray[i][j];
		}
		s+='\n';
	}
	alert(s);
}

window.onload = function() {
  $('#board').hide();
}
