// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static it.frob.sleighidea.psi.SleighTypes.*;

%%

%{
  public SleighLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class SleighLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

SPACE=[ \t\n\x0B\f\r]+
COMMENT=#.*
DECNUMBER=[0-9]+
DISPLAYCHAR=[@$?]
HEXNUMBER=0x[\da-fA-F]+
BINNUMBER=0b[01]+
LITERALSYMBOL=[_a-zA-Z][a-zA-Z\d_.]*
QSTRING=('([^'\\]|\\.)*'|\"([^\"\\]|\\.)*\")
WHITESPACE=[ \t\r\n]

%%
<YYINITIAL> {
  {WHITE_SPACE}         { return WHITE_SPACE; }

  "*"                   { return ASTERISK; }
  "="                   { return ASSIGN; }
  ";"                   { return SEMI; }
  "_"                   { return UNDERSCORE; }
  "("                   { return LPAREN; }
  ")"                   { return RPAREN; }
  ","                   { return COMMA; }
  "["                   { return LBRACKET; }
  "]"                   { return RBRACKET; }
  "{"                   { return LBRACE; }
  "}"                   { return RBRACE; }
  ":"                   { return COLON; }
  "..."                 { return ELLIPSIS; }
  "!"                   { return EXCLAIM; }
  "~"                   { return TILDE; }
  "=="                  { return EQUAL; }
  "!="                  { return NOTEQUAL; }
  "<"                   { return LESS; }
  ">"                   { return GREAT; }
  "<="                  { return LESSEQUAL; }
  ">="                  { return GREATEQUAL; }
  "||"                  { return BOOL_OR; }
  "^^"                  { return BOOL_XOR; }
  "&&"                  { return BOOL_AND; }
  "|"                   { return PIPE; }
  "^"                   { return CARET; }
  "&"                   { return AMPERSAND; }
  "<<"                  { return LEFT; }
  ">>"                  { return RIGHT; }
  "+"                   { return PLUS; }
  "-"                   { return MINUS; }
  "/"                   { return SLASH; }
  "%"                   { return PERCENT; }
  "$or"                 { return SPEC_OR; }
  "$and"                { return SPEC_AND; }
  "$xor"                { return SPEC_XOR; }
  "f=="                 { return FEQUAL; }
  "f!="                 { return FNOTEQUAL; }
  "f<"                  { return FLESS; }
  "f>"                  { return FGREAT; }
  "f<="                 { return FLESSEQUAL; }
  "f>="                 { return FGREATEQUAL; }
  "f+"                  { return FPLUS; }
  "f-"                  { return FMINUS; }
  "f*"                  { return FMULT; }
  "f/"                  { return FDIV; }
  "s<"                  { return SLESS; }
  "s>"                  { return SGREAT; }
  "s<="                 { return SLESSEQUAL; }
  "s>="                 { return SGREATEQUAL; }
  "s>>"                 { return SRIGHT; }
  "s/"                  { return SDIV; }
  "s%"                  { return SREM; }
  "if"                  { return RES_IF; }
  "is"                  { return RES_IS; }
  "with"                { return RES_WITH; }
  "alignment"           { return KEY_ALIGNMENT; }
  "attach"              { return KEY_ATTACH; }
  "big"                 { return KEY_BIG; }
  "bitrange"            { return KEY_BITRANGE; }
  "build"               { return KEY_BUILD; }
  "call"                { return KEY_CALL; }
  "context"             { return KEY_CONTEXT; }
  "crossbuild"          { return KEY_CROSSBUILD; }
  "dec"                 { return KEY_DEC; }
  "default"             { return KEY_DEFAULT; }
  "define"              { return KEY_DEFINE; }
  "endian"              { return KEY_ENDIAN; }
  "export"              { return KEY_EXPORT; }
  "goto"                { return KEY_GOTO; }
  "hex"                 { return KEY_HEX; }
  "little"              { return KEY_LITTLE; }
  "local"               { return KEY_LOCAL; }
  "macro"               { return KEY_MACRO; }
  "names"               { return KEY_NAMES; }
  "noflow"              { return KEY_NOFLOW; }
  "offset"              { return KEY_OFFSET; }
  "pcodeop"             { return KEY_PCODEOP; }
  "return"              { return KEY_RETURN; }
  "signed"              { return KEY_SIGNED; }
  "size"                { return KEY_SIZE; }
  "space"               { return KEY_SPACE; }
  "token"               { return KEY_TOKEN; }
  "type"                { return KEY_TYPE; }
  "unimpl"              { return KEY_UNIMPL; }
  "values"              { return KEY_VALUES; }
  "variables"           { return KEY_VARIABLES; }
  "wordsize"            { return KEY_WORDSIZE; }
  "ram_space"           { return BUILTIN_RAM_SPACE; }
  "rom_space"           { return BUILTIN_ROM_SPACE; }
  "register_space"      { return BUILTIN_REGISTER_SPACE; }
  "$("                  { return DEFINITION_START; }
  "@define"             { return KEY_ATDEFINE; }
  "@if"                 { return KEY_ATIF; }
  "@ifdef"              { return KEY_ATIFDEF; }
  "@ifndef"             { return KEY_ATIFNDEF; }
  "@include"            { return KEY_ATINCLUDE; }
  "@elif"               { return KEY_ATELIF; }
  "@else"               { return KEY_ATELSE; }
  "@endif"              { return KEY_ATENDIF; }
  "defined"             { return KEY_DEFINED; }

  {SPACE}               { return SPACE; }
  {COMMENT}             { return COMMENT; }
  {DECNUMBER}           { return DECNUMBER; }
  {DISPLAYCHAR}         { return DISPLAYCHAR; }
  {HEXNUMBER}           { return HEXNUMBER; }
  {BINNUMBER}           { return BINNUMBER; }
  {LITERALSYMBOL}       { return LITERALSYMBOL; }
  {QSTRING}             { return QSTRING; }
  {WHITESPACE}          { return WHITESPACE; }

}

[^] { return BAD_CHARACTER; }
