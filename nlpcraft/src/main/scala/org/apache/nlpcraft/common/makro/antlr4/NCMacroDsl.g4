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
syn : (TXT | REGEX_TXT | IDL_TXT);
group: LCURLY list RCURLY MINMAX?;
list
    : expr
    | list VERT expr
    | list VERT UNDERSCORE
    | UNDERSCORE VERT list
    ;

// Lexer.
LCURLY: '{';
RCURLY: '}';
VERT: '|';
COMMA: ',';
UNDERSCORE: '_';
fragment ESC_CHAR: [{}\\_[\]|,];
fragment ESC: '\\' ESC_CHAR;
fragment TXT_CHAR
    : [~!@#$%^&*()+._]
    | [-=<>/\\;:`'",]
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
    | '\u1F01'..'\u1FFF' /* Greek Extended. */
    | '\u0400'..'\u04FF' /* Cyrillic. */
    | '\u200C'..'\u200D'
    | '\u2070'..'\u218F'
    | '\u2C00'..'\u2FEF'
    | '\u3001'..'\uD7FF'
    | '\uF900'..'\uFDCF'
    | '\uFDF0'..'\uFFFD'
    ; // Ignoring ['\u10000-'\uEFFFF].
MINMAX: '[' [ 0-9,]+ ']';
REGEX_TXT: '//' .*? '//';
IDL_TXT: '^^' .*? '^^';
TXT: (TXT_CHAR | ESC)+;
WS: [ \r\t\u000C\n]+ -> skip ;
ERR_CHAR: .;