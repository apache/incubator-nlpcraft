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
    | NUM
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
term
    : dslTerm
    | clsTerm
    ;
dslTerm: 'term' termId? termEq LCURLY item RCURLY minMax?;
clsTerm: 'term' termId? termEq FSLASH clsNer FSLASH minMax?;
clsNer: javaFqn? POUND ID;
javaFqn
    : ID
    | javaFqn DOT ID
    ;
termId: LPAREN ID RPAREN;
item
    : pred
    | LPAREN item RPAREN
    | item (AND | OR) item
    | EXCL item
    ;
pred: expr PRED_OP expr;
expr
    : val
    | LPAREN expr RPAREN
    | expr mathOp expr
    | ID LPAREN expr? RPAREN // Buit-in function call.
    ;
mathOp
    : MINUS
    | PLUS
    | STAR
    | FSLASH
    ;
val
    : singleVal
    | LPAREN val RPAREN
    ;




singleVal
    : NULL
    | NUM
    | BOOL
    | qstring
    | tokQual? ('id' | 'aliases' | 'startidx' | 'endidx' | 'parent' | 'groups' | 'ancestors' | 'value')
    | tokQual? tokMeta
    | modelMeta
    | intentMeta
    ;
tokQual
    : tokQualPart
    | tokQual tokQualPart
    ;
tokQualPart: ID DOT;
tokMeta // Token metadata: ~prop
    : TILDA ID
    | TILDA ID LBR INT RBR
    | TILDA ID LBR qstring RBR
    ;
modelMeta // Model metadata: #prop
    : POUND ID
    | POUND ID LBR INT RBR
    | POUND ID LBR qstring RBR
    ;
intentMeta // Intent metadata: $prop
    : PERCENT ID
    | PERCENT ID LBR INT RBR
    | PERCENT ID LBR qstring RBR
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
POWER: '^';
BOOL: 'true' | 'false';
NULL: 'null';
INT: '0' | [1-9][_0-9]*;
NUM: MINUS? INT (DOT [0-9]+)? ([Ee][+\-]? INT)?;
ID: (UNDERSCORE|[a-z]|[A-Z]|[$])+([$]|[a-z]|[A-Z]|[0-9]|COLON|MINUS|UNDERSCORE)*;
WS: [ \r\t\u000C\n]+ -> skip ;

ErrorCharacter
  : .
  ;
