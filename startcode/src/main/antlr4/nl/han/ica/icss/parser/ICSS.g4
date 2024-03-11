grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
stylesheet: variable+ class+;
class: classID OPEN_BRACE rule+ CLOSE_BRACE;
classID: LOWER_IDENT | ID_IDENT | CLASS_IDENT;
rule: property | ifStatement;
ifStatement: IF BOX_BRACKET_OPEN CAPITAL_IDENT BOX_BRACKET_CLOSE OPEN_BRACE rule+ CLOSE_BRACE (ELSE OPEN_BRACE rule+ CLOSE_BRACE)?;
property: LOWER_IDENT COLON value SEMICOLON;
value: COLOR | width| TRUE | FALSE | CAPITAL_IDENT;
variable: CAPITAL_IDENT ASSIGNMENT_OPERATOR value SEMICOLON;
width: (SCALAR | PIXELSIZE | PERCENTAGE | CAPITAL_IDENT) ((PLUS | MIN | MUL) (SCALAR | PIXELSIZE))*;