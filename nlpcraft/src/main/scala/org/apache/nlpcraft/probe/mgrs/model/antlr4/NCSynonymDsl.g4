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

grammar NCSynonymDsl;

synonym
    : alias LPAREN item RPAREN EOF
    | item EOF
    ;
alias: LBR ID RBR;
item
    : pred
    | LPAREN item RPAREN
    | item (AND | OR) item
    | EXCL item
    ;
pred: expr PRED_OP expr;
expr
    : val
    | ID LPAREN expr RPAREN // Buit-in function call.
    ;
val
    : singleVal
    | LPAREN val RPAREN
    | val COMMA val
    ;
singleVal
    : 'null'
    | MINUS? (INT | INT EXP)
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
    : DOLLAR ID
    | DOLLAR ID LBR INT RBR
    | DOLLAR ID LBR qstring RBR
    ;
qstring
    : SQSTRING
    | DQSTRING
    ;
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
EXCL: '!';
LPAREN: '(';
RPAREN: ')';
SQUOTE: '\'';
DQUOTE: '"';
DOLLAR: '$';
TILDA: '~';
LBR: '[';
RBR: ']';
COMMA: ',';
COLON: ':';
POUND: '#';
MINUS: '-';
DOT: '.';
UNDERSCORE: '_';
BOOL: 'true' | 'false';
INT: '0' | [1-9][_0-9]*;
EXP: DOT [0-9]+;
ID: (UNDERSCORE|[a-z]|[A-Z])+([a-z]|[A-Z]|[0-9]|COLON|MINUS|UNDERSCORE)*;
WS : [ \r\t\u000C\n]+ -> skip ;

ErrorCharacter
  : .
  ;
