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
stylesheet: variableAssignment* styleRule* ;

//The 2 line below create a variable such as: LinkColor := #ff0000;
variableReference: CAPITAL_IDENT ;
variableAssignment: variableReference ASSIGNMENT_OPERATOR expression SEMICOLON ;

//A style rule is a selector followed by a rule body such as: p { color: #ff0000; }
styleRule: selector OPEN_BRACE ruleBody CLOSE_BRACE ;

//A rule body is what is between {} in a styleRule
ruleBody: (declaration | ifClause | variableAssignment)+ ;

//A selector is a reference to a class, id or tag such as: h1, .id, #tag
selector: LOWER_IDENT | CLASS_IDENT | ID_IDENT ;

//A declaration is a property followed by a value such as: color: #ff0000;
declaration: LOWER_IDENT COLON expression SEMICOLON ;

//An if clause is a conditional statement such as: if [ChanceColor] { color: #ffffff } else { [color: #0000ff] }
ifClause: IF BOX_BRACKET_OPEN variableReference BOX_BRACKET_CLOSE OPEN_BRACE ruleBody CLOSE_BRACE elseClause? ;
elseClause: ELSE OPEN_BRACE ruleBody CLOSE_BRACE ;

//An expression is the equivalent of a formula in css such as: 10px + 20px
expression: literal | expression (MUL) expression | expression (PLUS | MIN) expression ;

//A literal is a single value such as: #ff0000, 10px, 10%
literal: COLOR | PIXELSIZE | PERCENTAGE | SCALAR | booleanLiteral | variableReference ;

//The boolean literal is a true or false value
booleanLiteral: TRUE | FALSE ;