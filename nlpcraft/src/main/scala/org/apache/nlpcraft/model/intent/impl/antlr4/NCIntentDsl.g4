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

intent: intentId orderedDecl? flowDecl? metaDecl? terms EOF;
intentId: 'intent' ASSIGN ID;
orderedDecl: 'ordered' ASSIGN BOOL;
flowDecl: 'flow' ASSIGN qstring;
metaDecl: 'meta' ASSIGN jsonObj;
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
terms: term | terms term;
termEq
    : ASSIGN // Do not use conversation.
    | TILDA // Use conversation.
    ;
term: 'term' termId? termEq ((LBRACE expr RBRACE) | (DIV clsNer DIV)) minMax?;
clsNer: javaFqn? POUND ID;
javaFqn
    : ID
    | javaFqn DOT ID
    ;
termId: LPAR ID RPAR;
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
    | ID LPAR paramList? RPAR # callExpr
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
SQSTRING: SQUOTE (~'\'')* SQUOTE;
DQSTRING: DQUOTE (~'"')* DQUOTE;
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
ID: (UNDERSCORE|[a-z]|[A-Z]|DOLLAR)+(DOLLAR|[a-z]|[A-Z]|[0-9]|COLON|MINUS|UNDERSCORE)*;
WS: [ \r\t\u000C\n]+ -> skip;

ErrorCharacter
  : .
  ;
