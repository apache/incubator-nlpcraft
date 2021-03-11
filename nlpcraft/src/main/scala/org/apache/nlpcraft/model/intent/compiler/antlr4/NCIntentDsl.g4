/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar NCIntentDsl;

// Parser.
dsl: dslItems EOF; // Intent DSL enty point.
synonym: alias? LBRACE expr RBRACE EOF; // Synonym DSL entry point.
alias: LBR id RBR;
dslItems
    : dslItem
    | dslItems dslItem
    ;
dslItem
    : intent // Intent declaration.
    | frag // Fragment declaration.
    | imp // External URL containing DSL declarations (recursive parsing).
    ;
imp: 'import' LPAR qstring RPAR;
frag: fragId terms;
fragId: FRAG ASSIGN id;
fragRef: FRAG LPAR id fragMeta? RPAR;
fragMeta: COMMA jsonObj;
intent: intentId orderedDecl? flowDecl? metaDecl? terms;
intentId: 'intent' ASSIGN id;
orderedDecl: 'ordered' ASSIGN BOOL;
mtdDecl: DIV mtdRef DIV;
flowDecl: 'flow' ASSIGN (qstring | mtdDecl);
metaDecl: 'meta' ASSIGN jsonObj;
jsonObj
    : LBRACE jsonPair (COMMA jsonPair)* RBRACE
    |
     LBRACE RBRACE
    ;
jsonPair: qstring COLON jsonVal;
jsonVal
    : qstring
    | MINUS? INT REAL? EXP?
    | jsonObj
    | jsonArr
    | BOOL
    | NULL
    ;
jsonArr
    : LBR jsonVal (COMMA jsonVal)* RBR
    | LBR RBR
    ;
terms
    : termItem
    | terms termItem;
termItem
    : term
    | fragRef
    ;
termEq
    : ASSIGN // Do not use conversation.
    | TILDA // Use conversation.
    ;
term: 'term' termId? termEq ((LBRACE expr RBRACE) | mtdDecl) minMax?;
mtdRef: javaFqn? POUND id;
javaFqn
    : id
    | javaFqn DOT id
    ;
termId: LPAR id RPAR;
expr
    // NOTE: order of productions defines precedence.
    : op=(MINUS | NOT) expr # unaryExpr
    | LPAR expr RPAR # parExpr
    | expr op=(MULT | DIV | MOD) expr # multExpr
    | expr op=(PLUS | MINUS) expr # plusExpr
    | expr op=(LTEQ | GTEQ | LT | GT) expr # compExpr
    | expr op=(EQ | NEQ) expr # eqExpr
    | expr op=(AND | OR) expr # logExpr
    | atom # atomExpr
    | FUN_NAME LPAR paramList? RPAR # callExpr
    ;
paramList
    : expr
    | paramList COMMA expr
    ;
atom
    : NULL
    | INT REAL? EXP?
    | BOOL
    | qstring
    ;
qstring
    : SQSTRING
    | DQSTRING
    ;
minMax
    : minMaxShortcut
    | minMaxRange
    ;
minMaxShortcut
    : PLUS
    | QUESTION
    | MULT
    ;
minMaxRange: LBR INT COMMA INT RBR;
id
    : ID
    | FUN_NAME // Function name can overlap with ID so we detect both.
    ;

// Lexer.
FUN_NAME
    : 'meta_token'
    | 'meta_part'
    | 'meta_model'
    | 'meta_intent'
    | 'meta_req'
    | 'meta_user'
    | 'meta_company'
    | 'meta_sys'
    | 'meta_conv'
    | 'meta_frag'
    | 'json'
    | 'if'
    | 'id'
    | 'this'
    | 'part'
    | 'parts'
    | 'ancestors'
    | 'parent'
    | 'groups'
    | 'value'
    | 'aliases'
    | 'start_idx'
    | 'end_idx'
    | 'req_id'
    | 'req_normtext'
    | 'req_tstamp'
    | 'req_addr'
    | 'req_agent'
    | 'user_id'
    | 'user_fname'
    | 'user_lname'
    | 'user_email'
    | 'user_admin'
    | 'user_signup_tstamp'
    | 'comp_id'
    | 'comp_name'
    | 'comp_website'
    | 'comp_country'
    | 'comp_region'
    | 'comp_city'
    | 'comp_addr'
    | 'comp_postcode'
    | 'trim'
    | 'strip'
    | 'uppercase'
    | 'lowercase'
    | 'is_alpha'
    | 'is_alphanum'
    | 'is_whitespace'
    | 'is_num'
    | 'is_numspace'
    | 'is_alphaspace'
    | 'is_alphanumspace'
    | 'substring'
    | 'charAt'
    | 'regex'
    | 'soundex'
    | 'split'
    | 'split_trim'
    | 'replace'
    | 'abs'
    | 'ceil'
    | 'floor'
    | 'rint'
    | 'round'
    | 'signum'
    | 'sqrt'
    | 'cbrt'
    | 'pi'
    | 'euler'
    | 'acos'
    | 'asin'
    | 'atan'
    | 'cos'
    | 'sin'
    | 'tan'
    | 'cosh'
    | 'sinh'
    | 'tanh'
    | 'atn2'
    | 'degrees'
    | 'radians'
    | 'exp'
    | 'expm1'
    | 'hypot'
    | 'log'
    | 'log10'
    | 'log1p'
    | 'pow'
    | 'rand'
    | 'square'
    | 'list'
    | 'map'
    | 'get'
    | 'index'
    | 'has'
    | 'tail'
    | 'add'
    | 'remove'
    | 'first'
    | 'last'
    | 'keys'
    | 'values'
    | 'length'
    | 'count'
    | 'take'
    | 'drop'
    | 'size'
    | 'reverse'
    | 'is_empty'
    | 'non_empty'
    | 'to_string'
    | 'avg'
    | 'max'
    | 'min'
    | 'stdev'
    | 'sum'
    | 'year'
    | 'month'
    | 'day_of_month'
    | 'day_of_week'
    | 'day_of_year'
    | 'hour'
    | 'minute'
    | 'second'
    | 'week_of_month'
    | 'week_of_year'
    | 'quarter'
    | 'now'
    ;
FRAG: 'fragment'; // To resolve ambiguity with ANTLR4 keyword.
SQSTRING: SQUOTE ((~'\'') | ('\\''\''))* SQUOTE; // Allow for \' (escaped single quote) in the string.
DQSTRING: DQUOTE ((~'"') | ('\\''"'))* DQUOTE; // Allow for \" (escape double quote) in the string.
BOOL: 'true' | 'false';
NULL: 'null';
EQ: '==';
NEQ: '!=';
GTEQ: '>=';
LTEQ: '<=';
GT: '>';
LT: '<';
AND: '&&';
OR: '||';
VERT: '|';
NOT: '!';
LPAR: '(';
RPAR: ')';
LBRACE: '{';
RBRACE: '}';
SQUOTE: '\'';
DQUOTE: '"';
TILDA: '~';
LBR: '[';
RBR: ']';
POUND: '#';
COMMA: ',';
COLON: ':';
MINUS: '-';
DOT: '.';
UNDERSCORE: '_';
ASSIGN: '=';
PLUS: '+';
QUESTION: '?';
MULT: '*';
DIV: '/';
MOD: '%';
DOLLAR: '$';
INT: '0' | [1-9] [_0-9]*;
REAL: DOT [0-9]+;
EXP: [Ee] [+\-]? INT;
fragment UNI_CHAR // International chars.
    : '\u0300'..'\u036F'
    | '\u00A0'..'\u00FF' /* Latin-1 Supplement. */
    | '\u0100'..'\u017F' /* Latin Extended-A. */
    | '\u0180'..'\u024F' /* Latin Extended-B. */
    | '\u1E02'..'\u1EF3' /* Latin Extended Additional. */
    | '\u0259'..'\u0292' /* IPA Extensions. */
    | '\u02B0'..'\u02FF' /* Spacing modifier letters. */
    | '\u203F'..'\u2040'
    | '\u1F01'..'\u1FFF' /* Greek Extended. */
    | '\u0400'..'\u04FF' /* Cyrillic. */
    | '\u200C'..'\u200D'
    | '\u2070'..'\u218F'
    | '\u2C00'..'\u2FEF'
    | '\u3001'..'\uD7FF'
    | '\uF900'..'\uFDCF'
    | '\uFDF0'..'\uFFFD'
    ; // Ignoring ['\u10000-'\uEFFFF].
fragment LETTER: [a-zA-Z];
ID: (UNI_CHAR|UNDERSCORE|LETTER|DOLLAR)+(UNI_CHAR|DOLLAR|LETTER|[0-9]|COLON|MINUS|UNDERSCORE)*;
COMMENT : ('//' ~[\r\n]* '\r'? ('\n'| EOF) | '/*' .*? '*/' ) -> skip;
WS: [ \r\t\u000C\n]+ -> skip;
ErrorChar: .;
