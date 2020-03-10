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

intent: intentId convDecl? orderedDecl? flowDecl? terms EOF;
intentId: 'intent' EQ ID;
convDecl: 'conv' EQ BOOL;
orderedDecl: 'ordered' EQ BOOL;
flowDecl: 'flow' EQ SQUOTE flow SQUOTE;
flow
    :
    | flowItem
    | flow RIGHT flowItem
    ;
flowItem: flowItemIds minMax?;
flowItemIds
    : ID
    | LPAREN idList RPAREN
    ;
idList
    : ID
    | idList VERT ID
    ;
terms: term | terms term;
term: 'term' termId? EQ LCURLY item RCURLY minMax?;
termId: LPAREN ID RPAREN;
item
    : predicate
    | LPAREN item RPAREN
    | item (AND | OR) item
    | EXCL item
    ;
predicate
    : lval PRED_OP rval
    | ID LPAREN lval RPAREN PRED_OP rval  // Function call.
    ;
lval: lvalQual? ('id' | 'aliases' | 'startidx' | 'endidx' | 'parent' | 'groups' | 'ancestors' | 'value' | meta);
lvalQual: lvalPart | lvalQual lvalPart;
lvalPart: ID DOT;
rvalSingle
    : 'null'
    | MINUS? (INT | INT EXP)
    | BOOL
    | qstring
    ;
rval
    : rvalSingle
    | LPAREN rvalList RPAREN
    ;
rvalList
    : rvalSingle
    | rvalList COMMA rvalSingle
    ;
meta
    : TILDA ID
    | TILDA ID LBR INT RBR
    | TILDA ID LBR qstring RBR
    ;
qstring: SQUOTE ~'\''* SQUOTE;
minMax: minMaxShortcut | minMaxRange;
minMaxShortcut: PLUS | QUESTION | STAR;
minMaxRange: LBR INT COMMA INT RBR;
PRED_OP: '==' | '!=' | '>=' | '<=' | '>' | '<' | '@@' | '!@';
AND: '&&';
OR: '||';
VERT: '|';
EXCL: '!';
LPAREN: '(';
RPAREN: ')';
LCURLY: '{';
RCURLY: '}';
SQUOTE: '\'';
TILDA: '~';
RIGHT: '>>';
LBR: '[';
RBR: ']';
COMMA: ',';
COLON: ':';
MINUS: '-';
DOT: '.';
UNDERSCORE: '_';
EQ: '=';
PLUS: '+';
QUESTION: '?';
STAR: '*';
BOOL: 'true' | 'false';
INT: '0' | [1-9][_0-9]*;
EXP: DOT [0-9]+;
ID: (UNDERSCORE|[a-z]|[A-Z])+([a-z]|[A-Z]|[0-9]|COLON|MINUS|UNDERSCORE)*;
WS : [ \r\t\u000C\n]+ -> skip ;

ErrorCharacter
  : .
  ;
