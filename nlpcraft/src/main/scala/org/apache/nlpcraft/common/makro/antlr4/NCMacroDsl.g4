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

// Parser.
makro: expr EOF;
expr
    : item
    | expr item
    ;
item: syn | group;
syn : (TXT | INT | REGEX_TXT | DSL_TXT); // NOTE: since TXT and INT overlap - we catch them both here and resolve in compiler.
group: LCURLY list RCURLY minMax?;
minMax: LBR INT COMMA INT RBR;
list
    : expr
    | list VERT expr
    | list VERT UNDERSCORE
    | UNDERSCORE VERT list
    ;

// Lexer.
LCURLY: '{';
RCURLY: '}';
LBR: '[';
RBR: ']';
VERT: '|';
COMMA: ',';
UNDERSCORE: '_';
fragment ESC_CHAR: [{}\\_[\]|,];
fragment ESC: '\\' ESC_CHAR;
fragment TXT_CHAR
    : [~!@#$%^&*()+.]
    | [-=<>/\\;:`'"]
    | '\u00B7'
    | 'A'..'Z'
    | 'a'..'z'
    | '0'..'9'
    | '\u0300'..'\u036F'
    | '\u00A0'..'\u00FF' /* Latin-1 Supplement. */
    | '\u0100'..'\u017F' /* Latin Extended-A. */
    | '\u0180'..'\u024F' /* Latin Extended-B. */
    | '\u1E02'..'\u1EF3' /* Latin Extended Additional. */
    | '\u0259'..'\u0292' /* IPA Extensions. */
    | '\u02B0'..'\u02FF' /* Spacing modifier letters. */
    | '\u203F'..'\u2040'
    | '\u00C0'..'\u00D6'
    | '\u00D8'..'\u00F6'
    | '\u00F8'..'\u02FF'
    | '\u0370'..'\u03FF' /* Greek and Coptic. */
    | '\u1F01'..'\u1FFF' /* Greek Extended. */
    | '\u0400'..'\u04FF' /* Cyrillic. */
    | '\u037F'..'\u1FFF'
    | '\u200C'..'\u200D'
    | '\u2070'..'\u218F'
    | '\u2C00'..'\u2FEF'
    | '\u3001'..'\uD7FF'
    | '\uF900'..'\uFDCF'
    | '\uFDF0'..'\uFFFD'
    ; // Ignoring ['\u10000-'\uEFFFF].
INT: '0' | [1-9][_0-9]*;
REGEX_TXT: '//' .*? '//';
DSL_TXT: '^^' .*? '^^';
TXT: (TXT_CHAR | ESC)+;
WS: [ \r\t\u000C\n]+ -> skip ;
ERR_CHAR: .;