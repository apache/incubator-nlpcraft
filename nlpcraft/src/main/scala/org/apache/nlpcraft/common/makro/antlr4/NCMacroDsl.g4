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

grammar NCMacroDsl;

line
    : syn
    | line syn
    ;
syn
    : (TXT | INT) // NOTE: since TXT and INT overlap - we catch them both here and resolve in compiler.
    | group
    ;
group: LCURLY list RCURLY minMax?;
minMax: LBR INT COMMA INT RBR;
list
    : syn
    | list VERT (syn | UNDERSCORE)
    ;
LCURLY: '{';
RCURLY: '}';
LBR: '[';
RBR: ']';
VERT: '|';
COMMA: ',';
UNDERSCORE: '_';
fragment TXT_CHAR
    : [.*^+<>\-&'":#!]
    | '\u00B7'
    | '\u0300'..'\u036F'
    | '\u203F'..'\u2040'
    | 'A'..'Z'
    | 'a'..'z'
    | '0'..'9'
    | '\u00C0'..'\u00D6'
    | '\u00D8'..'\u00F6'
    | '\u00F8'..'\u02FF'
    | '\u0370'..'\u037D'
    | '\u037F'..'\u1FFF'
    | '\u200C'..'\u200D'
    | '\u2070'..'\u218F'
    | '\u2C00'..'\u2FEF'
    | '\u3001'..'\uD7FF'
    | '\uF900'..'\uFDCF'
    | '\uFDF0'..'\uFFFD'
    ; // Ignoring ['\u10000-'\uEFFFF].
fragment ESC: '\\' [{}\\<>_[\]|,] ;
INT: '0' | [1-9][_0-9]*;
TXT: (TXT_CHAR | ESC)+;
WS: [ \r\t\u000C\n]+ -> skip ;
ErrorCharacter: .;
