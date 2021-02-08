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
intentId: 'intent' EQ ID;
orderedDecl: 'ordered' EQ BOOL;
flowDecl: 'flow' EQ qstring;
metaDecl: 'meta' EQ jsonObj;
jsonObj
    : LCURLY jsonPair (COMMA jsonPair)* RCURLY
    | LCURLY RCURLY
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
    : EQ // Do not use conversation.
    | TILDA // Use conversation.
    ;
term: 'term' termId? termEq ((LCURLY termDef RCURLY) | (FSLASH clsNer FSLASH)) minMax?;
clsNer: javaFqn? POUND ID;
javaFqn
    : ID
    | javaFqn DOT ID
    ;
termId: LPAREN ID RPAREN;
termDef
    : termPred
    | LPAREN termDef RPAREN
    | termDef (AND | OR) termDef
    | EXCL termDef
    ;
termPred: expr PRED_OP expr;
expr
    : val
    | expr COMMA val
    | LPAREN expr RPAREN
    | expr (MINUS | PLUS | STAR | FSLASH) expr
    | ID LPAREN expr? RPAREN // Buit-in function call.
    ;
val
    : NULL
    | MINUS? INT REAL? EXP?
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
    | STAR
    ;
minMaxRange: LBR INT COMMA INT RBR;
SQSTRING: SQUOTE (~'\'')* SQUOTE;
DQSTRING: DQUOTE (~'"')* DQUOTE;
PRED_OP
    : '==' // Includes regex for strings.
    | '!=' // Includes regex for strings.
    | '>='
    | '<='
    | '>'
    | '<'
    | '@@' // Set or string containment.
    | '!@' // Set or string not containment.
    ;
AND: '&&';
OR: '||';
VERT: '|';
EXCL: '!';
LPAREN: '(';
RPAREN: ')';
LCURLY: '{';
RCURLY: '}';
SQUOTE: '\'';
DQUOTE: '"';
TILDA: '~';
RIGHT: '>>';
LBR: '[';
RBR: ']';
POUND: '#';
COMMA: ',';
COLON: ':';
MINUS: '-';
DOT: '.';
UNDERSCORE: '_';
EQ: '=';
PLUS: '+';
QUESTION: '?';
STAR: '*';
FSLASH: '/';
PERCENT: '%';
DOLLAR: '$';
POWER: '^';
BOOL: 'true' | 'false';
NULL: 'null';
INT: '0' | [1-9] [_0-9]*;
REAL: DOT [0-9]+;
EXP: [Ee] [+\-]? INT;
ID: (UNDERSCORE|[a-z]|[A-Z]|DOLLAR)+(DOLLAR|[a-z]|[A-Z]|[0-9]|COLON|MINUS|UNDERSCORE)*;
WS: [ \r\t\u000C\n]+ -> skip;

ErrorCharacter
  : .
  ;
