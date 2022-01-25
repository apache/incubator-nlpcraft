/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the '
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an '
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar NCIDL;

// Parser.
idl: idlDecls EOF; // Intent enty point.
idlDecls
    : idlDecl
    | idlDecls idlDecl
    ;
idlDecl
    : intent // Intent declaration.
    | frag // Fragment declaration.
    | imprt // External URL containing IDL declarations (recursive parsing).
    ;
imprt: 'import' LPAR qstring RPAR;
frag: fragId termDecls;
fragId: FRAG ASSIGN id;
fragRef: FRAG LPAR id fragMeta? RPAR;
fragMeta: COMMA jsonObj;
intent: intentId optDecl? flowDecl? metaDecl? termDecls;
intentId: 'intent' ASSIGN id;
flowDecl: 'flow' ASSIGN qstring;
metaDecl: 'meta' ASSIGN jsonObj;
optDecl: 'options' ASSIGN jsonObj;
jsonObj
    : LBRACE jsonPair (COMMA jsonPair)* RBRACE
    | LBRACE RBRACE
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
termDecls
    : termDecl
    | termDecls termDecl;
termDecl
    : term
    | fragRef
    ;
termEq
    : ASSIGN // Do not use conversation.
    | TILDA // Use conversation.
    ;
term: 'term' termId? termEq LBRACE vars? expr RBRACE minMax?;
mtdRef: javaFqn? POUND id;
javaFqn
    : javaClass
    | javaFqn DOT javaClass
    ;
javaClass
    : id
    // We need to include keywords to make sure they don't conflict.
    | IMPORT
    | INTENT
    | OPTIONS
    | FLOW
    | META
    | TERM
    | FRAG
    ;
termId: LPAR id RPAR;
expr
    // NOTE: order of productions defines precedence.
    : op=(MINUS | NOT) expr # unaryExpr
    | LPAR expr RPAR # parExpr
    | expr op=(MULT | DIV | MOD) expr # multDivModExpr
    | expr op=(PLUS | MINUS) expr # plusMinusExpr
    | expr op=(LTEQ | GTEQ | LT | GT) expr # compExpr
    | expr op=(EQ | NEQ) expr # eqNeqExpr
    | expr op=(AND | OR) expr # andOrExpr
    | atom # atomExpr
    | (FUN_NAME | POUND) LPAR paramList? RPAR # callExpr
    | (FUN_NAME | POUND) # callExpr
    | AT id # varRef
    ;
vars
    : varDecl
    | vars varDecl
    ;
varDecl: AT id ASSIGN expr;
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
    : 'meta_ent'
    | 'meta_cfg'
    | 'meta_intent'
    | 'meta_req'
    | 'meta_sys'
    | 'meta_conv'
    | 'meta_frag'
    | 'json'
    | 'if'
    | 'ent_id'
    | 'ent_index'
    | 'ent_text'
    | 'ent_groups'
    | 'ent_count'
    | 'ent_all'
    | 'ent_all_for_id'
    | 'ent_all_for_group'
    | 'ent_this'
    | 'ent_is_last'
    | 'ent_is_first'
    | 'ent_is_before_id'
    | 'ent_is_before_group'
    | 'ent_is_after_id'
    | 'ent_is_after_group'
    | 'ent_is_between_ids'
    | 'ent_is_between_groups'
    | 'mdl_id'
    | 'mdl_name'
    | 'mdl_version'
    | 'mdl_origin'
    | 'req_id'
    | 'req_text'
    | 'req_tstamp'
    | 'user_id'
    | 'trim'
    | 'regex'
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
    | 'split'
    | 'split_trim'
    | 'starts_with'
    | 'ends_with'
    | 'index_of'
    | 'contains'
    | 'substr'
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
    | 'to_double'
    | 'to_int'
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
    | 'atan2'
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
    | 'get'
    | 'has'
    | 'has_any'
    | 'has_all'
    | 'first'
    | 'last'
    | 'keys'
    | 'values'
    | 'length'
    | 'count'
    | 'size'
    | 'sort'
    | 'reverse'
    | 'is_empty'
    | 'non_empty'
    | 'distinct'
    | 'concat'
    | 'to_string'
    | 'max'
    | 'min'
    | 'avg'
    | 'stdev'
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
    | 'or_else'
    ;

IMPORT : 'import' ;
INTENT : 'intent' ;
OPTIONS : 'options' ;
FLOW : 'flow' ;
META : 'meta' ;
TERM : 'term' ;
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
AT: '@';
DOLLAR: '$';
INT: '0' | [1-9] [_0-9]*;
REAL: DOT [0-9]+;
EXP: [Ee] [+\-]? INT;
fragment UNI_CHAR // International chars.
    : ~[\u0000-\u007F\uD800-\uDBFF] // Covers all characters above 0x7F which are not a surrogate.
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // Covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF.
    ;
fragment LETTER: [a-zA-Z];
ID: (UNI_CHAR|UNDERSCORE|LETTER|DOLLAR)+(UNI_CHAR|DOLLAR|LETTER|[0-9]|COLON|MINUS|UNDERSCORE)*;
COMMENT : ('//' ~[\r\n]* '\r'? ('\n'| EOF) | '/*' .*? '*/' ) -> skip;
WS: [ \r\t\u000C\n]+ -> skip;
ErrorChar: .;
