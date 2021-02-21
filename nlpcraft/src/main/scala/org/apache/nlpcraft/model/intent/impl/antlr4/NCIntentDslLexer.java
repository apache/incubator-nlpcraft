// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4\NCIntentDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.impl.antlr4;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCIntentDslLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, SQSTRING=6, DQSTRING=7, BOOL=8, 
		NULL=9, EQ=10, NEQ=11, GTEQ=12, LTEQ=13, GT=14, LT=15, AND=16, OR=17, 
		VERT=18, NOT=19, LPAR=20, RPAR=21, LBRACE=22, RBRACE=23, SQUOTE=24, DQUOTE=25, 
		TILDA=26, LBR=27, RBR=28, POUND=29, COMMA=30, COLON=31, MINUS=32, DOT=33, 
		UNDERSCORE=34, ASSIGN=35, PLUS=36, QUESTION=37, MULT=38, DIV=39, MOD=40, 
		DOLLAR=41, INT=42, REAL=43, EXP=44, ID=45, WS=46, ErrorCharacter=47;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "SQSTRING", "DQSTRING", "BOOL", 
			"NULL", "EQ", "NEQ", "GTEQ", "LTEQ", "GT", "LT", "AND", "OR", "VERT", 
			"NOT", "LPAR", "RPAR", "LBRACE", "RBRACE", "SQUOTE", "DQUOTE", "TILDA", 
			"LBR", "RBR", "POUND", "COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", 
			"ASSIGN", "PLUS", "QUESTION", "MULT", "DIV", "MOD", "DOLLAR", "INT", 
			"REAL", "EXP", "UNI_CHAR", "LETTER", "ID", "WS", "ErrorCharacter"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'meta'", "'term'", null, null, 
			null, "'null'", "'=='", "'!='", "'>='", "'<='", "'>'", "'<'", "'&&'", 
			"'||'", "'|'", "'!'", "'('", "')'", "'{'", "'}'", "'''", "'\"'", "'~'", 
			"'['", "']'", "'#'", "','", "':'", "'-'", "'.'", "'_'", "'='", "'+'", 
			"'?'", "'*'", "'/'", "'%'", "'$'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "SQSTRING", "DQSTRING", "BOOL", "NULL", 
			"EQ", "NEQ", "GTEQ", "LTEQ", "GT", "LT", "AND", "OR", "VERT", "NOT", 
			"LPAR", "RPAR", "LBRACE", "RBRACE", "SQUOTE", "DQUOTE", "TILDA", "LBR", 
			"RBR", "POUND", "COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", "ASSIGN", 
			"PLUS", "QUESTION", "MULT", "DIV", "MOD", "DOLLAR", "INT", "REAL", "EXP", 
			"ID", "WS", "ErrorCharacter"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public NCIntentDslLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "NCIntentDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\61\u0126\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\3\2\3\2\3\2\3"+
		"\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\5"+
		"\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\7\7\u0088\n\7\f\7"+
		"\16\7\u008b\13\7\3\7\3\7\3\b\3\b\3\b\3\b\7\b\u0093\n\b\f\b\16\b\u0096"+
		"\13\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u00a3\n\t\3\n\3"+
		"\n\3\n\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\16\3"+
		"\17\3\17\3\20\3\20\3\21\3\21\3\21\3\22\3\22\3\22\3\23\3\23\3\24\3\24\3"+
		"\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3"+
		"\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3"+
		"$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3+\7+\u00f3\n+\f+\16+\u00f6"+
		"\13+\5+\u00f8\n+\3,\3,\6,\u00fc\n,\r,\16,\u00fd\3-\3-\5-\u0102\n-\3-\3"+
		"-\3.\3.\3/\3/\3\60\3\60\3\60\3\60\6\60\u010e\n\60\r\60\16\60\u010f\3\60"+
		"\3\60\3\60\3\60\3\60\3\60\3\60\7\60\u0119\n\60\f\60\16\60\u011c\13\60"+
		"\3\61\6\61\u011f\n\61\r\61\16\61\u0120\3\61\3\61\3\62\3\62\2\2\63\3\3"+
		"\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21"+
		"!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!"+
		"A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[\2]\2_/a\60c\61\3\2\f\3\2))\3\2$$\3\2\63"+
		";\4\2\62;aa\3\2\62;\4\2GGgg\4\2--//\16\2\u00b9\u00b9\u00c2\u00d8\u00da"+
		"\u00f8\u00fa\u037f\u0381\u2001\u200e\u200f\u2041\u2042\u2072\u2191\u2c02"+
		"\u2ff1\u3003\ud801\uf902\ufdd1\ufdf2\uffff\4\2C\\c|\5\2\13\f\16\17\"\""+
		"\2\u0138\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2"+
		"\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2"+
		"\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2"+
		"\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2"+
		"\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S"+
		"\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2"+
		"\2\2\3e\3\2\2\2\5l\3\2\2\2\7t\3\2\2\2\ty\3\2\2\2\13~\3\2\2\2\r\u0083\3"+
		"\2\2\2\17\u008e\3\2\2\2\21\u00a2\3\2\2\2\23\u00a4\3\2\2\2\25\u00a9\3\2"+
		"\2\2\27\u00ac\3\2\2\2\31\u00af\3\2\2\2\33\u00b2\3\2\2\2\35\u00b5\3\2\2"+
		"\2\37\u00b7\3\2\2\2!\u00b9\3\2\2\2#\u00bc\3\2\2\2%\u00bf\3\2\2\2\'\u00c1"+
		"\3\2\2\2)\u00c3\3\2\2\2+\u00c5\3\2\2\2-\u00c7\3\2\2\2/\u00c9\3\2\2\2\61"+
		"\u00cb\3\2\2\2\63\u00cd\3\2\2\2\65\u00cf\3\2\2\2\67\u00d1\3\2\2\29\u00d3"+
		"\3\2\2\2;\u00d5\3\2\2\2=\u00d7\3\2\2\2?\u00d9\3\2\2\2A\u00db\3\2\2\2C"+
		"\u00dd\3\2\2\2E\u00df\3\2\2\2G\u00e1\3\2\2\2I\u00e3\3\2\2\2K\u00e5\3\2"+
		"\2\2M\u00e7\3\2\2\2O\u00e9\3\2\2\2Q\u00eb\3\2\2\2S\u00ed\3\2\2\2U\u00f7"+
		"\3\2\2\2W\u00f9\3\2\2\2Y\u00ff\3\2\2\2[\u0105\3\2\2\2]\u0107\3\2\2\2_"+
		"\u010d\3\2\2\2a\u011e\3\2\2\2c\u0124\3\2\2\2ef\7k\2\2fg\7p\2\2gh\7v\2"+
		"\2hi\7g\2\2ij\7p\2\2jk\7v\2\2k\4\3\2\2\2lm\7q\2\2mn\7t\2\2no\7f\2\2op"+
		"\7g\2\2pq\7t\2\2qr\7g\2\2rs\7f\2\2s\6\3\2\2\2tu\7h\2\2uv\7n\2\2vw\7q\2"+
		"\2wx\7y\2\2x\b\3\2\2\2yz\7o\2\2z{\7g\2\2{|\7v\2\2|}\7c\2\2}\n\3\2\2\2"+
		"~\177\7v\2\2\177\u0080\7g\2\2\u0080\u0081\7t\2\2\u0081\u0082\7o\2\2\u0082"+
		"\f\3\2\2\2\u0083\u0089\5\61\31\2\u0084\u0088\n\2\2\2\u0085\u0086\7^\2"+
		"\2\u0086\u0088\7)\2\2\u0087\u0084\3\2\2\2\u0087\u0085\3\2\2\2\u0088\u008b"+
		"\3\2\2\2\u0089\u0087\3\2\2\2\u0089\u008a\3\2\2\2\u008a\u008c\3\2\2\2\u008b"+
		"\u0089\3\2\2\2\u008c\u008d\5\61\31\2\u008d\16\3\2\2\2\u008e\u0094\5\63"+
		"\32\2\u008f\u0093\n\3\2\2\u0090\u0091\7^\2\2\u0091\u0093\7$\2\2\u0092"+
		"\u008f\3\2\2\2\u0092\u0090\3\2\2\2\u0093\u0096\3\2\2\2\u0094\u0092\3\2"+
		"\2\2\u0094\u0095\3\2\2\2\u0095\u0097\3\2\2\2\u0096\u0094\3\2\2\2\u0097"+
		"\u0098\5\63\32\2\u0098\20\3\2\2\2\u0099\u009a\7v\2\2\u009a\u009b\7t\2"+
		"\2\u009b\u009c\7w\2\2\u009c\u00a3\7g\2\2\u009d\u009e\7h\2\2\u009e\u009f"+
		"\7c\2\2\u009f\u00a0\7n\2\2\u00a0\u00a1\7u\2\2\u00a1\u00a3\7g\2\2\u00a2"+
		"\u0099\3\2\2\2\u00a2\u009d\3\2\2\2\u00a3\22\3\2\2\2\u00a4\u00a5\7p\2\2"+
		"\u00a5\u00a6\7w\2\2\u00a6\u00a7\7n\2\2\u00a7\u00a8\7n\2\2\u00a8\24\3\2"+
		"\2\2\u00a9\u00aa\7?\2\2\u00aa\u00ab\7?\2\2\u00ab\26\3\2\2\2\u00ac\u00ad"+
		"\7#\2\2\u00ad\u00ae\7?\2\2\u00ae\30\3\2\2\2\u00af\u00b0\7@\2\2\u00b0\u00b1"+
		"\7?\2\2\u00b1\32\3\2\2\2\u00b2\u00b3\7>\2\2\u00b3\u00b4\7?\2\2\u00b4\34"+
		"\3\2\2\2\u00b5\u00b6\7@\2\2\u00b6\36\3\2\2\2\u00b7\u00b8\7>\2\2\u00b8"+
		" \3\2\2\2\u00b9\u00ba\7(\2\2\u00ba\u00bb\7(\2\2\u00bb\"\3\2\2\2\u00bc"+
		"\u00bd\7~\2\2\u00bd\u00be\7~\2\2\u00be$\3\2\2\2\u00bf\u00c0\7~\2\2\u00c0"+
		"&\3\2\2\2\u00c1\u00c2\7#\2\2\u00c2(\3\2\2\2\u00c3\u00c4\7*\2\2\u00c4*"+
		"\3\2\2\2\u00c5\u00c6\7+\2\2\u00c6,\3\2\2\2\u00c7\u00c8\7}\2\2\u00c8.\3"+
		"\2\2\2\u00c9\u00ca\7\177\2\2\u00ca\60\3\2\2\2\u00cb\u00cc\7)\2\2\u00cc"+
		"\62\3\2\2\2\u00cd\u00ce\7$\2\2\u00ce\64\3\2\2\2\u00cf\u00d0\7\u0080\2"+
		"\2\u00d0\66\3\2\2\2\u00d1\u00d2\7]\2\2\u00d28\3\2\2\2\u00d3\u00d4\7_\2"+
		"\2\u00d4:\3\2\2\2\u00d5\u00d6\7%\2\2\u00d6<\3\2\2\2\u00d7\u00d8\7.\2\2"+
		"\u00d8>\3\2\2\2\u00d9\u00da\7<\2\2\u00da@\3\2\2\2\u00db\u00dc\7/\2\2\u00dc"+
		"B\3\2\2\2\u00dd\u00de\7\60\2\2\u00deD\3\2\2\2\u00df\u00e0\7a\2\2\u00e0"+
		"F\3\2\2\2\u00e1\u00e2\7?\2\2\u00e2H\3\2\2\2\u00e3\u00e4\7-\2\2\u00e4J"+
		"\3\2\2\2\u00e5\u00e6\7A\2\2\u00e6L\3\2\2\2\u00e7\u00e8\7,\2\2\u00e8N\3"+
		"\2\2\2\u00e9\u00ea\7\61\2\2\u00eaP\3\2\2\2\u00eb\u00ec\7\'\2\2\u00ecR"+
		"\3\2\2\2\u00ed\u00ee\7&\2\2\u00eeT\3\2\2\2\u00ef\u00f8\7\62\2\2\u00f0"+
		"\u00f4\t\4\2\2\u00f1\u00f3\t\5\2\2\u00f2\u00f1\3\2\2\2\u00f3\u00f6\3\2"+
		"\2\2\u00f4\u00f2\3\2\2\2\u00f4\u00f5\3\2\2\2\u00f5\u00f8\3\2\2\2\u00f6"+
		"\u00f4\3\2\2\2\u00f7\u00ef\3\2\2\2\u00f7\u00f0\3\2\2\2\u00f8V\3\2\2\2"+
		"\u00f9\u00fb\5C\"\2\u00fa\u00fc\t\6\2\2\u00fb\u00fa\3\2\2\2\u00fc\u00fd"+
		"\3\2\2\2\u00fd\u00fb\3\2\2\2\u00fd\u00fe\3\2\2\2\u00feX\3\2\2\2\u00ff"+
		"\u0101\t\7\2\2\u0100\u0102\t\b\2\2\u0101\u0100\3\2\2\2\u0101\u0102\3\2"+
		"\2\2\u0102\u0103\3\2\2\2\u0103\u0104\5U+\2\u0104Z\3\2\2\2\u0105\u0106"+
		"\t\t\2\2\u0106\\\3\2\2\2\u0107\u0108\t\n\2\2\u0108^\3\2\2\2\u0109\u010e"+
		"\5[.\2\u010a\u010e\5E#\2\u010b\u010e\5]/\2\u010c\u010e\5S*\2\u010d\u0109"+
		"\3\2\2\2\u010d\u010a\3\2\2\2\u010d\u010b\3\2\2\2\u010d\u010c\3\2\2\2\u010e"+
		"\u010f\3\2\2\2\u010f\u010d\3\2\2\2\u010f\u0110\3\2\2\2\u0110\u011a\3\2"+
		"\2\2\u0111\u0119\5[.\2\u0112\u0119\5S*\2\u0113\u0119\5]/\2\u0114\u0119"+
		"\t\6\2\2\u0115\u0119\5? \2\u0116\u0119\5A!\2\u0117\u0119\5E#\2\u0118\u0111"+
		"\3\2\2\2\u0118\u0112\3\2\2\2\u0118\u0113\3\2\2\2\u0118\u0114\3\2\2\2\u0118"+
		"\u0115\3\2\2\2\u0118\u0116\3\2\2\2\u0118\u0117\3\2\2\2\u0119\u011c\3\2"+
		"\2\2\u011a\u0118\3\2\2\2\u011a\u011b\3\2\2\2\u011b`\3\2\2\2\u011c\u011a"+
		"\3\2\2\2\u011d\u011f\t\13\2\2\u011e\u011d\3\2\2\2\u011f\u0120\3\2\2\2"+
		"\u0120\u011e\3\2\2\2\u0120\u0121\3\2\2\2\u0121\u0122\3\2\2\2\u0122\u0123"+
		"\b\61\2\2\u0123b\3\2\2\2\u0124\u0125\13\2\2\2\u0125d\3\2\2\2\21\2\u0087"+
		"\u0089\u0092\u0094\u00a2\u00f4\u00f7\u00fd\u0101\u010d\u010f\u0118\u011a"+
		"\u0120\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}