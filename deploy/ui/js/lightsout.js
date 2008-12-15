// constants
var EMPTY = ' ';
var FILLED = 'X';

// custom representation of the board, will be different for different games
var currentBoard;
var defaultBoard = [];

// easy reference to these constants for yourself (it's static for now, but we might want this to be user-defined later)
var width = 4;
var height = 4;

// used for coloring the table cells
var moveValueClasses = ['lose-move', 'tie-move', 'win-move'];

// other state
var nextMoves = [];
var lastMove = -1;

// bootstrapping function - start up this program after the page structure loads
//$(document).ready(function(){
function createboard(val) {
    var s = parseInt(val);
    if (!(s > 0)) {
        return;
    }
    width = height = s;
    $("#boardSizeInput").hide();
    // create a new game
    var game = GCWeb.newPuzzleGame("lightsout", width, height, {
        onNextValuesReceived: onNextValuesReceived,
        isValidMove: isValidMove,
        onExecutingMove: onExecutingMove,
        updateMoveValues: updateMoveValues, 
        clearMoveValues: clearMoveValues,
        getPositionValue: getPositionValue,
        getNextMoveValues: getNextMoveValues,
        maxRemoteness: -1,
        options: {size: width},
        debug: 0
    });
    for (var row = 0; row < height; row++) {
        defaultBoard[row] = []
        for (var col = 0; col < width; col++) {
            defaultBoard[row][col] = EMPTY;
        }
    }
    // load the default board
    game.loadBoard(getBoardString(defaultBoard));
    currentBoard = defaultBoard;
   
    var gametable = document.getElementById("gametable"); 
    for(var row=0;row<height;row++) {
        var htmlrow = document.createElement("tr");
        gametable.appendChild(htmlrow);
        for(var col=0;col<width;col++) {
            var htmlcell = document.createElement("td");
            htmlcell.setAttribute("id", "cell-"+row+"-"+col);
            htmlcell.appendChild(document.createTextNode(defaultBoard[row][col]));
            htmlrow.appendChild(htmlcell);
            // what happens when you click a table cell
            $('#cell-'+row+'-'+col).click(function(row, col){
                return function(){
                    // find the move information that we stored and attempt to execute the move
                    for(i=0;i<nextMoves.length;i++){
                        if(row == height-nextMoves[i].move[1] && col == nextMoves[i].move.charCodeAt(0)-'a'.charCodeAt(0)){
                            game.doMove(nextMoves[i]);
							
                        }
                    }
                }
            }(row, col));
        }
    }
}
//);

// check to see whether the current move is valid
function isValidMove(moveInfo)
{
    return true;
}

// called when doMove executes successfully
function onExecutingMove(moveInfo){
    // update our own state
    lastMove = moveInfo.board;

    // update the graphical display
    for(row=0;row<height;row++) {
        for(col=0;col<width;col++) {
            var s = lastMove.charAt(row*width+col);
            if (s == '1' || s == FILLED) {
                s = FILLED;
            } else {
                s = EMPTY;
            }
            currentBoard[row][col] = s;
            $('#cell-'+row+'-'+col).text(currentBoard[row][col]);
        }
    }
}

// called on initial load, and each subsequent doMove will also reference this
function onNextValuesReceived(json){
    nextMoves = json;
}

// colors the board based on move values
function updateMoveValues(nextMoves){
    // reset everything first
    clearMoveValues();
    
    // set background color to new values
    for(i=0;i<nextMoves.length;i++) {
        // if the move were something like a3, then you would use the commented lines below instead
        row = height-nextMoves[i].move[1];
        col = nextMoves[i].move.charCodeAt(0)-'a'.charCodeAt(0);
        
        // else if the move is a simple integer (in the case of 1210 puzzle), then the col is always 0 and the row is just the move number
        //row = nextMoves[i].move;
        //col = 0;
        
        // adds the css class to the table cell depending on whether it's a lose, draw, or win
        $('#cell-'+row+'-'+col).addClass(moveValueClasses[nextMoves[i].value-1]);
    }
}

// remove all indicators of move values
function clearMoveValues(){
    // clear background color
    for(row=0;row<height;row++) {
        for(col=0;col<width;col++) {
            // resets the css classes on this table cell
            $('#cell-'+row+'-'+col).removeClass();
        }
    }
}

// converts our own representation of the board (2d/3d array) into a board string
function getBoardString(board){
    var str = '';
    for(row=0;row<height;row++) {
        for(col=0;col<width;col++) {
            str += (board[row][col] == FILLED)?'1':'0';
        }
    }
    return str;
}

// local debugging
function getPositionValue(position, onValueReceived){
    onValueReceived({
        "board": position, 
        "move": null, 
        "remoteness": "5",
        "value": 3
    });
    return;
}

function getNextMoveValues(position, onMoveValuesReceived){
    var retval = [];
	var moveString = '';
    for(row=0;row<height;row++) {
		for(col=0;col<width;col++) {
			moveString += String.fromCharCode(col +'a'.charCodeAt(0));
			moveString += height-row;
			
			retval.push({"board": "", "move": moveString, "remoteness": -1, "status": "OK", "value": 3});
        }
    }

    

    onMoveValuesReceived(retval);
}