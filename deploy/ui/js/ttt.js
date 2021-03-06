var EMPTY = ' ';
var currentBoard = [[EMPTY,EMPTY,EMPTY],[EMPTY,EMPTY,EMPTY],[EMPTY,EMPTY,EMPTY]];
var pieces = ['X', 'O'];
var currentPlayer = 0;
var width = 3;
var height = 3;
var meanings = ['','Lose','Draw','Win'];
var moveValueColors = ['', '#8a0000', '#ff0', '#0f0'];
var moveValueClasses = ['', 'lose-move', 'tie-move', 'win-move'];

$(document).ready(function(){
    var game = GCWeb.newGame("ttt", width, height, {});
    updateBoard(game, currentBoard);
    
    for(var row=0;row<height;row++) {
        for(var col=0;col<width;col++) {
            $('#cell-'+row+'-'+col).click(function(row, col){
                return function(){
                    if(currentBoard[row][col] == EMPTY){
                        currentBoard[row][col] = pieces[currentPlayer];
                        currentPlayer = (currentPlayer+1)%2;
                        updateBoard(game, currentBoard);
                    }
                }
            }(row, col));
        }
    }
});

function updateBoard(game, newBoard) {
    for(row=0;row<height;row++) {
        for(col=0;col<width;col++) {
            $('#cell-'+row+'-'+col).text(newBoard[row][col]);
        }
    }
    game.getPositionValue(getBoardString(newBoard), function(json){
        $('#current-value').text('Current Value: '+meanings[json.value]);
    });
    game.getNextMoveValues(getBoardString(newBoard), function(json){
        // clear background color
        for(row=0;row<height;row++) {
            for(col=0;col<width;col++) {
                $('#cell-'+row+'-'+col).removeClass();
            }
        }
        // set background color to new values
        for(i=0;i<json.length;i++) {
            row = height-json[i].move[1];
            col = json[i].move.charCodeAt(0)-'a'.charCodeAt(0);
            $('#cell-'+row+'-'+col).addClass(moveValueClasses[json[i].value]);
        }
    });
    $('#turn').text("It's "+pieces[currentPlayer]+"'s turn!");
}

function getBoardString(board){
    var str = '';
    for(row=0;row<height;row++) {
        for(col=0;col<width;col++) {
            str += board[row][col];
        }
    }
    return str;
}